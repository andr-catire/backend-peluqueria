package peluqueria.servicio;

import java.sql.SQLException;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import peluqueria.configuracion.PropiedadesHorario;
import peluqueria.excepcion.ExcepcionReglaNegocio;
import peluqueria.modelo.Asistencia;
import peluqueria.modelo.Estilista;
import peluqueria.repositorio.RepositorioAsistencia;
import peluqueria.repositorio.RepositorioEstilista;
import peluqueria.transferencia.AsistenciaHoyRespuesta;
import peluqueria.transferencia.AsistenciaPersonalRespuesta;

/**
 * Reglas de entrada y salida del personal y consulta diaria del administrador. La hora sale del
 * reloj del servidor; el navegador solo muestra el estado calculado aquí.
 */
@Service
public class GestorAsistencia {

    private static final Logger log = LoggerFactory.getLogger(GestorAsistencia.class);
    private static final String SQLSTATE_UNICO = "23505";

    private final RepositorioAsistencia repositorioAsistencia;
    private final RepositorioEstilista repositorioEstilista;
    private final GestorHorario gestorHorario;
    private final Clock reloj;

    public GestorAsistencia(
            RepositorioAsistencia repositorioAsistencia,
            RepositorioEstilista repositorioEstilista,
            GestorHorario gestorHorario,
            Clock reloj) {
        this.repositorioAsistencia = repositorioAsistencia;
        this.repositorioEstilista = repositorioEstilista;
        this.gestorHorario = gestorHorario;
        this.reloj = reloj;
    }

    /** Lista a todos los estilistas activos con su asistencia de la fecha indicada. */
    @Transactional(readOnly = true)
    public List<AsistenciaPersonalRespuesta> listarPorFecha(LocalDate fecha) {
        LocalDate dia = fecha == null ? LocalDate.now(reloj) : fecha;
        Map<Long, Asistencia> asistencias = repositorioAsistencia.findByFecha(dia).stream()
                .collect(Collectors.toMap(a -> a.getPersonal().getId(), a -> a));

        return repositorioEstilista.findByActivoTrueOrderByNombreAsc().stream()
                .map(estilista -> {
                    Asistencia asistencia = asistencias.get(estilista.getId());
                    if (asistencia == null) {
                        return new AsistenciaPersonalRespuesta(
                                estilista.getId(), estilista.getNombre(), estilista.getEmail(),
                                null, null, false, 0, false, null, false);
                    }
                    return new AsistenciaPersonalRespuesta(
                            estilista.getId(), estilista.getNombre(), estilista.getEmail(),
                            asistencia.getHoraEntrada(), asistencia.getHoraSalida(),
                            asistencia.isLlegoTarde(), asistencia.getMinutosTarde(),
                            asistencia.isSalidaAnticipada(), asistencia.getMotivoSalida(), true);
                })
                .toList();
    }

    /** Devuelve el estado de la jornada de hoy y qué acciones permite el horario. */
    @Transactional(readOnly = true)
    public AsistenciaHoyRespuesta obtenerHoy(Estilista personal) {
        PropiedadesHorario horario = gestorHorario.actual();
        LocalDate hoy = LocalDate.now(reloj);
        LocalTime ahora = LocalTime.now(reloj);
        Asistencia asistencia = buscar(hoy, personal.getId()).orElse(null);
        return construirRespuesta(hoy, ahora, asistencia, horario);
    }

    /** Registra la entrada una sola vez al día y marca el retraso respecto de la apertura. */
    @Transactional
    public AsistenciaHoyRespuesta registrarEntrada(Estilista personal) {
        PropiedadesHorario horario = gestorHorario.actual();
        LocalDate hoy = LocalDate.now(reloj);
        LocalTime ahora = LocalTime.now(reloj);

        if (buscar(hoy, personal.getId()).isPresent()) {
            throw entradaYaRegistrada();
        }

        if (ahora.isAfter(horario.cierre())) {
            throw ExcepcionReglaNegocio.conflicto(
                    "ENTRADA_FUERA_DE_HORARIO",
                    "Ya pasó la hora de cierre del local. No se puede registrar la entrada.");
        }

        Asistencia asistencia = new Asistencia();
        asistencia.setPersonal(personal);
        asistencia.setFecha(hoy);
        asistencia.setHoraEntrada(ahora);
        asistencia.setMinutosTarde(calcularMinutosTarde(ahora, horario));

        try {
            asistencia = repositorioAsistencia.saveAndFlush(asistencia);
        } catch (DataIntegrityViolationException excepcion) {
            if (esViolacionUnica(excepcion)) {
                throw entradaYaRegistrada();
            }
            throw excepcion;
        }

        log.info("AUDITORIA accion=ASISTENCIA_ENTRADA usuario={} tarde={}",
                personal.getEmail(), asistencia.isLlegoTarde());

        return construirRespuesta(hoy, ahora, asistencia, horario);
    }

    /** Registra la salida normal desde el cierre o una anticipada con motivo, solo una vez. */
    @Transactional
    public AsistenciaHoyRespuesta registrarSalida(Estilista personal, String motivo) {
        PropiedadesHorario horario = gestorHorario.actual();
        LocalDate hoy = LocalDate.now(reloj);
        LocalTime ahora = LocalTime.now(reloj);

        Asistencia asistencia = buscar(hoy, personal.getId())
                .orElseThrow(() -> ExcepcionReglaNegocio.conflicto(
                        "SIN_ENTRADA", "Primero debes registrar tu entrada."));

        if (asistencia.getHoraSalida() != null) {
            throw salidaYaRegistrada();
        }

        boolean anticipada = ahora.isBefore(horario.cierre());
        String motivoLimpio = normalizarMotivo(motivo);

        if (anticipada && motivoLimpio == null) {
            throw ExcepcionReglaNegocio.invalida(
                    "MOTIVO_REQUERIDO",
                    "Para salir antes del cierre debes indicar un motivo de al menos 5 caracteres.");
        }
        if (anticipada && motivoLimpio.length() < 5) {
            throw ExcepcionReglaNegocio.invalida(
                    "MOTIVO_REQUERIDO",
                    "El motivo debe tener al menos 5 caracteres.");
        }

        if (!anticipada) {
            motivoLimpio = null;
        }

        int filas = repositorioAsistencia.registrarSalida(
                asistencia.getId(), ahora, anticipada, motivoLimpio);

        if (filas == 0) {
            throw salidaYaRegistrada();
        }

        log.info("AUDITORIA accion=ASISTENCIA_SALIDA usuario={} anticipada={}",
                personal.getEmail(), anticipada);

        Asistencia actualizada = buscar(hoy, personal.getId()).orElse(asistencia);
        return construirRespuesta(hoy, ahora, actualizada, horario);
    }

    private Optional<Asistencia> buscar(LocalDate fecha, Long personalId) {
        return repositorioAsistencia.findByPersonalIdAndFecha(personalId, fecha);
    }

    /** Minutos desde la apertura cuando se supera la tolerancia; 0 si llegó a tiempo. */
    private int calcularMinutosTarde(LocalTime ahora, PropiedadesHorario horario) {
        LocalTime limite = horario.apertura().plusMinutes(horario.toleranciaMinutos());
        if (!ahora.isAfter(limite)) {
            return 0;
        }
        return (int) Duration.between(horario.apertura(), ahora).toMinutes();
    }

    private AsistenciaHoyRespuesta construirRespuesta(
            LocalDate fecha, LocalTime ahora, Asistencia asistencia, PropiedadesHorario horario) {

        boolean hayEntrada = asistencia != null;
        boolean haySalida = hayEntrada && asistencia.getHoraSalida() != null;
        boolean puedeEntrada = !hayEntrada && !ahora.isAfter(horario.cierre());
        boolean puedeSalida = hayEntrada && !haySalida && !ahora.isBefore(horario.cierre());

        OffsetDateTime salidaHabilitadaDesde = fecha.atTime(horario.cierre())
                .atZone(horario.zona())
                .toOffsetDateTime();

        return new AsistenciaHoyRespuesta(
                fecha,
                ahora,
                horario.apertura(),
                horario.cierre(),
                hayEntrada ? asistencia.getHoraEntrada() : null,
                haySalida ? asistencia.getHoraSalida() : null,
                hayEntrada && asistencia.isLlegoTarde(),
                hayEntrada ? asistencia.getMinutosTarde() : 0,
                hayEntrada && asistencia.isSalidaAnticipada(),
                puedeEntrada,
                puedeSalida,
                salidaHabilitadaDesde);
    }

    private static String normalizarMotivo(String motivo) {
        if (motivo == null) {
            return null;
        }
        String limpio = motivo.trim();
        return limpio.isEmpty() ? null : limpio;
    }

    private static boolean esViolacionUnica(DataIntegrityViolationException excepcion) {
        Throwable causa = excepcion.getMostSpecificCause();
        return causa instanceof SQLException sql
                && SQLSTATE_UNICO.equals(sql.getSQLState());
    }

    private static ExcepcionReglaNegocio entradaYaRegistrada() {
        return ExcepcionReglaNegocio.conflicto(
                "ENTRADA_YA_REGISTRADA", "Ya registraste tu entrada de hoy.");
    }

    private static ExcepcionReglaNegocio salidaYaRegistrada() {
        return ExcepcionReglaNegocio.conflicto(
                "SALIDA_YA_REGISTRADA", "Ya registraste tu salida de hoy.");
    }
}
