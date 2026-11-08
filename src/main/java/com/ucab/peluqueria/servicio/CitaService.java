package com.ucab.peluqueria.servicio;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ucab.peluqueria.excepcion.ExcepcionRecursoNoEncontrado;
import com.ucab.peluqueria.excepcion.ExcepcionReglaNegocio;
import com.ucab.peluqueria.modelo.Agenda;
import com.ucab.peluqueria.modelo.Cita;
import com.ucab.peluqueria.modelo.Cliente;
import com.ucab.peluqueria.modelo.EstadoCita;
import com.ucab.peluqueria.modelo.Estilista;
import com.ucab.peluqueria.modelo.Servicio;
import com.ucab.peluqueria.modelo.Usuario;
import com.ucab.peluqueria.repositorio.RepositorioAgenda;
import com.ucab.peluqueria.repositorio.RepositorioCita;
import com.ucab.peluqueria.repositorio.RepositorioCliente;
import com.ucab.peluqueria.repositorio.RepositorioEstilista;
import com.ucab.peluqueria.repositorio.RepositorioServicio;
import com.ucab.peluqueria.repositorio.RepositorioUsuario;
import com.ucab.peluqueria.transferencia.CitaRespuesta;
import com.ucab.peluqueria.transferencia.EstilistaResumenRespuesta;
import com.ucab.peluqueria.transferencia.SolicitudAgendarCita;

@Service
public class CitaService {

    private static final LocalTime APERTURA = LocalTime.of(9, 0);
    private static final LocalTime CIERRE = LocalTime.of(18, 0);
    private static final int INTERVALO_MINUTOS = 30;

    private final RepositorioCita repositorioCita;
    private final RepositorioAgenda repositorioAgenda;
    private final RepositorioServicio repositorioServicio;
    private final RepositorioEstilista repositorioEstilista;
    private final RepositorioCliente repositorioCliente;
    private final RepositorioUsuario repositorioUsuario;

    public CitaService(
            RepositorioCita repositorioCita,
            RepositorioAgenda repositorioAgenda,
            RepositorioServicio repositorioServicio,
            RepositorioEstilista repositorioEstilista,
            RepositorioCliente repositorioCliente,
            RepositorioUsuario repositorioUsuario) {
        this.repositorioCita = repositorioCita;
        this.repositorioAgenda = repositorioAgenda;
        this.repositorioServicio = repositorioServicio;
        this.repositorioEstilista = repositorioEstilista;
        this.repositorioCliente = repositorioCliente;
        this.repositorioUsuario = repositorioUsuario;
    }

    /** Lista los estilistas activos que ofrecen un servicio activo específico. */
    @Transactional(readOnly = true)
    public List<EstilistaResumenRespuesta> obtenerEstilistasPorServicio(Long servicioId) {
        repositorioServicio.findById(servicioId)
                .filter(Servicio::isActivo)
                .orElseThrow(() -> ExcepcionReglaNegocio.noEncontrado(
                        "NOT_FOUND", "El servicio no existe."));

        List<Object[]> filas = repositorioEstilista.buscarPorServicio(servicioId);
        List<EstilistaResumenRespuesta> resultado = new ArrayList<>();
        for (Object[] fila : filas) {
            Long id = ((Number) fila[0]).longValue();
            String nombre = (String) fila[1];
            String email = (String) fila[2];
            resultado.add(new EstilistaResumenRespuesta(id, nombre, email));
        }
        return resultado;
    }

    /** Calcula las horas disponibles (en formato HH:mm) para un estilista y fecha dados. */
    @Transactional(readOnly = true)
    public List<String> obtenerDisponibilidad(Long servicioId, Long estilistaId, LocalDate fecha, Long clienteId) {
        Servicio servicio = repositorioServicio.findById(servicioId)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("El servicio no existe."));

        if (!servicio.isActivo()) {
            return List.of();
        }

        // Si es domingo, cerrado
        if (fecha.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return List.of();
        }

        // No más de 7 días adelante ni fechas pasadas
        LocalDate hoy = LocalDate.now();
        if (fecha.isBefore(hoy) || fecha.isAfter(hoy.plusDays(7))) {
            return List.of();
        }

        int duracion = servicio.getDuracionMinutos();
        List<Cita> citasEstilista = repositorioCita.buscarPorEstilistaYFecha(estilistaId, fecha, EstadoCita.CANCELADA);
        List<Cita> citasCliente = clienteId != null
                ? repositorioCita.buscarPorClienteYFecha(clienteId, fecha, EstadoCita.CANCELADA)
                : List.of();

        List<String> disponibles = new ArrayList<>();
        LocalTime actual = APERTURA;
        LocalTime ahoraHora = LocalTime.now();

        while (!actual.plusMinutes(duracion).isAfter(CIERRE)) {
            LocalTime fin = actual.plusMinutes(duracion);

            // Si es hoy, no permitir horas que ya pasaron
            if (fecha.equals(hoy) && !actual.isAfter(ahoraHora.plusMinutes(10))) {
                actual = actual.plusMinutes(INTERVALO_MINUTOS);
                continue;
            }

            boolean conflictoEstilista = seSolapa(actual, fin, citasEstilista);
            boolean conflictoCliente = seSolapa(actual, fin, citasCliente);

            if (!conflictoEstilista && !conflictoCliente) {
                disponibles.add(actual.toString());
            }

            actual = actual.plusMinutes(INTERVALO_MINUTOS);
        }

        return disponibles;
    }

    /** Agenda una nueva cita. */
    @Transactional
    public CitaRespuesta agendar(SolicitudAgendarCita solicitud, Long clienteId) {
        LocalDate fecha = solicitud.fecha();
        LocalTime inicio = solicitud.horaInicio();

        LocalDate hoy = LocalDate.now();
        if (fecha.isBefore(hoy)) {
            throw ExcepcionReglaNegocio.invalida("PAST_DATE", "No puedes agendar en fechas pasadas.");
        }
        if (fecha.equals(hoy) && !inicio.isAfter(LocalTime.now().plusMinutes(5))) {
            throw ExcepcionReglaNegocio.invalida("PAST_TIME", "El horario seleccionado ya ha pasado para el día de hoy.");
        }
        if (fecha.isAfter(hoy.plusDays(7))) {
            throw ExcepcionReglaNegocio.invalida("DATE_OUT_OF_RANGE", "Solo se puede agendar citas con un máximo de 7 días de anticipación.");
        }
        if (fecha.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw ExcepcionReglaNegocio.invalida("CLOSED_DAY", "La peluquería permanece cerrada los domingos.");
        }

        Servicio servicio = repositorioServicio.findById(solicitud.servicioId())
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("El servicio no existe."));
        if (!servicio.isActivo()) {
            throw ExcepcionReglaNegocio.invalida("INACTIVE_SERVICE", "El servicio no se encuentra activo.");
        }

        Cliente cliente = repositorioCliente.findById(clienteId)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("No se encontró el perfil de cliente."));

        LocalTime fin = inicio.plusMinutes(servicio.getDuracionMinutos());
        if (inicio.isBefore(APERTURA) || fin.isAfter(CIERRE)) {
            throw ExcepcionReglaNegocio.invalida("OUT_OF_HOURS", "El servicio excede el horario de atención (09:00 a 18:00).");
        }

        // Validar que el estilista ofrece el servicio
        List<Servicio> serviciosEstilista = repositorioEstilista.buscarServicios(solicitud.estilistaId());
        boolean ofrece = serviciosEstilista.stream().anyMatch(s -> s.getId().equals(servicio.getId()));
        if (!ofrece) {
            throw ExcepcionReglaNegocio.invalida("SERVICE_NOT_OFFERED", "El estilista seleccionado no ofrece este servicio.");
        }

        // Validar conflicto de horario del estilista
        List<Cita> citasEstilista = repositorioCita.buscarPorEstilistaYFecha(solicitud.estilistaId(), fecha, EstadoCita.CANCELADA);
        if (seSolapa(inicio, fin, citasEstilista)) {
            throw ExcepcionReglaNegocio.conflicto("STYLIST_BUSY", "El estilista ya tiene una cita reservada en ese horario.");
        }

        // Validar conflicto de horario del cliente
        List<Cita> citasCliente = repositorioCita.buscarPorClienteYFecha(clienteId, fecha, EstadoCita.CANCELADA);
        if (seSolapa(inicio, fin, citasCliente)) {
            throw ExcepcionReglaNegocio.conflicto("CLIENT_BUSY", "Ya tienes una cita agendada en ese mismo horario.");
        }

        Usuario usuarioEstilista = repositorioUsuario.findById(solicitud.estilistaId())
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("No se encontró el estilista."));

        // Obtener o crear la agenda del día para el estilista
        Agenda agenda = repositorioAgenda.findByEstilistaIdAndFecha(solicitud.estilistaId(), fecha)
                .orElseGet(() -> {
                    Estilista estilista = new Estilista();
                    estilista.setId(usuarioEstilista.getId());
                    estilista.setNombre(usuarioEstilista.getNombre());
                    estilista.setEmail(usuarioEstilista.getEmail());
                    Agenda nueva = new Agenda();
                    nueva.setEstilista(estilista);
                    nueva.setFecha(fecha);
                    return repositorioAgenda.save(nueva);
                });

        if (agenda.getEstilista().getNombre() == null) {
            agenda.getEstilista().setNombre(usuarioEstilista.getNombre());
        }

        Cita cita = new Cita();
        cita.setAgenda(agenda);
        cita.setCliente(cliente);
        cita.setServicio(servicio);
        cita.setHoraInicio(inicio);
        cita.setHoraFin(fin);
        cita.setCosto(servicio.getCosto());
        cita.setEstado(EstadoCita.CONFIRMADA);

        Cita guardada = repositorioCita.save(cita);
        return CitaRespuesta.desde(guardada);
    }

    /** Lista las citas de un cliente. */
    @Transactional(readOnly = true)
    public List<CitaRespuesta> obtenerMisCitas(Long clienteId) {
        return repositorioCita.buscarPorCliente(clienteId)
                .stream()
                .map(CitaRespuesta::desde)
                .toList();
    }

    /** Lista las citas para la agenda semanal (Admin / Estilista). */
    @Transactional(readOnly = true)
    public List<CitaRespuesta> obtenerAgendaSemanal(LocalDate fechaInicio, LocalDate fechaFin, Long estilistaId) {
        List<Cita> citas = (estilistaId != null)
                ? repositorioCita.buscarSemanalPorEstilista(estilistaId, fechaInicio, fechaFin, EstadoCita.CANCELADA)
                : repositorioCita.buscarSemanal(fechaInicio, fechaFin, EstadoCita.CANCELADA);

        return citas.stream().map(CitaRespuesta::desde).toList();
    }

    private static boolean seSolapa(LocalTime inicio, LocalTime fin, List<Cita> citas) {
        for (Cita c : citas) {
            // Se solapa si inicio < c.fin Y fin > c.inicio
            if (inicio.isBefore(c.getHoraFin()) && fin.isAfter(c.getHoraInicio())) {
                return true;
            }
        }
        return false;
    }
}
