package peluqueria.servicio;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import peluqueria.excepcion.ExcepcionRecursoNoEncontrado;
import peluqueria.excepcion.ExcepcionReglaNegocio;
import peluqueria.modelo.Estilista;
import peluqueria.modelo.Servicio;
import peluqueria.repositorio.RepositorioEstilista;
import peluqueria.repositorio.RepositorioServicio;
import peluqueria.repositorio.RepositorioUsuario;
import peluqueria.transferencia.EstilistaRequest;
import peluqueria.transferencia.EstilistaRespuesta;

/**
 * Reglas de negocio del personal: alta, listado, perfil y desactivación de estilistas.
 * Lo llama ControladorEstilista; usa RepositorioEstilista y RepositorioUsuario.
 */
@Service
public class GestorEstilistas {

    private static final Logger log = LoggerFactory.getLogger(GestorEstilistas.class);
    private static final ZoneId ZONA_HORARIA = ZoneId.of("America/Caracas");

    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioEstilista repositorioEstilista;
    private final RepositorioServicio repositorioServicio;
    private final PasswordEncoder passwordEncoder;

    public GestorEstilistas(
            RepositorioUsuario repositorioUsuario,
            RepositorioEstilista repositorioEstilista,
            RepositorioServicio repositorioServicio,
            PasswordEncoder passwordEncoder) {
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioEstilista = repositorioEstilista;
        this.repositorioServicio = repositorioServicio;
        this.passwordEncoder = passwordEncoder;
    }

    /** Crea una cuenta nueva de estilista con rol PERSONAL y sus servicios. */
    @Transactional
    public EstilistaRespuesta crear(EstilistaRequest solicitud, String adminEmail) {
        String email = solicitud.email().trim().toLowerCase(Locale.ROOT);

        if (repositorioUsuario.existsByEmailIgnoreCase(email)) {
            throw ExcepcionReglaNegocio.conflicto(
                    "EMAIL_TAKEN", "Ya existe una cuenta con ese correo.");
        }

        List<Servicio> servicios = cargarServicios(solicitud.serviciosIds());

        Estilista estilista = new Estilista();
        estilista.setNombre(solicitud.nombre().trim());
        estilista.setEmail(email);
        estilista.setPassword(passwordEncoder.encode(solicitud.password()));
        estilista.setActivo(true);
        estilista.setServicios(new HashSet<>(servicios));

        estilista = repositorioEstilista.save(estilista);
        log.info("AUDITORIA accion=EMPLEADO_CREADO admin={} objetivo={}",
                adminEmail, estilista.getId());

        return EstilistaRespuesta.desde(estilista);
    }

    /** Lista los estilistas activos con sus servicios, ordenados por nombre. */
    @Transactional(readOnly = true)
    public List<EstilistaRespuesta> listar() {
        return repositorioEstilista.findByActivoTrueOrderByNombreAsc().stream()
                .map(EstilistaRespuesta::desde)
                .toList();
    }

    /** Devuelve el perfil del estilista autenticado. */
    @Transactional(readOnly = true)
    public EstilistaRespuesta obtenerMiPerfil(Long idUsuario) {
        Estilista estilista = repositorioEstilista.findById(idUsuario)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado(
                        "No se encontró el perfil del estilista."));

        if (!estilista.isActivo()) {
            throw new ExcepcionRecursoNoEncontrado("No se encontró el perfil del estilista.");
        }

        return EstilistaRespuesta.desde(estilista);
    }

    /** Desactiva la cuenta sin cambiar el rol ni borrar su historial. */
    @Transactional
    public void desvincularEstilista(Long idEstilista, String adminEmail) {
        Estilista estilista = repositorioEstilista.findById(idEstilista)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado(
                        "No existe el estilista que quieres desactivar."));

        LocalDate hoy = LocalDate.now(ZONA_HORARIA);
        if (repositorioEstilista.tieneCitasFuturasActivas(idEstilista, hoy)) {
            throw ExcepcionReglaNegocio.conflicto(
                    "PENDING_APPOINTMENTS",
                    "El estilista tiene citas pendientes o confirmadas. Reasígnalas o cancélalas antes de desactivarlo.");
        }

        estilista.setActivo(false);
        repositorioEstilista.save(estilista);
        log.info("AUDITORIA accion=EMPLEADO_DESACTIVADO admin={} objetivo={}",
                adminEmail, idEstilista);
    }

    private List<Servicio> cargarServicios(Set<Long> ids) {
        List<Servicio> servicios = repositorioServicio.findAllById(ids);

        if (servicios.size() != ids.size()) {
            Set<Long> encontrados = servicios.stream()
                    .map(Servicio::getId)
                    .collect(Collectors.toSet());
            String faltantes = ids.stream()
                    .filter(id -> !encontrados.contains(id))
                    .map(String::valueOf)
                    .sorted()
                    .collect(Collectors.joining(", "));
            throw new ExcepcionRecursoNoEncontrado(
                    "No existen los servicios: " + faltantes + ".");
        }

        for (Servicio servicio : servicios) {
            if (!servicio.isActivo()) {
                throw ExcepcionReglaNegocio.conflicto(
                        "INACTIVE_SERVICE",
                        "El servicio " + servicio.getNombre() + " no está activo.");
            }
        }

        return servicios;
    }
}
