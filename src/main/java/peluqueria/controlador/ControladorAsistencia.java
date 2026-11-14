package peluqueria.controlador;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import peluqueria.modelo.Estilista;
import peluqueria.seguridad.UsuarioActual;
import peluqueria.servicio.GestorAsistencia;
import peluqueria.transferencia.AsistenciaHoyRespuesta;
import peluqueria.transferencia.AsistenciaPersonalRespuesta;
import peluqueria.transferencia.SolicitudSalida;

import jakarta.validation.Valid;

/**
 * Asistencia: autoservicio del personal y consulta diaria del administrador.
 * La identidad del empleado sale de la sesión, nunca de la URL ni del cuerpo.
 */
@RestController
@RequestMapping("/api/asistencia")
public class ControladorAsistencia {

    private final GestorAsistencia gestorAsistencia;
    private final UsuarioActual usuarioActual;

    public ControladorAsistencia(GestorAsistencia gestorAsistencia, UsuarioActual usuarioActual) {
        this.gestorAsistencia = gestorAsistencia;
        this.usuarioActual = usuarioActual;
    }

    /** Lista la asistencia de todos los estilistas activos para una fecha. Solo ADMIN. */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AsistenciaPersonalRespuesta> listar(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return gestorAsistencia.listarPorFecha(fecha);
    }

    /** Estado de la jornada de hoy del empleado autenticado. */
    @GetMapping("/hoy")
    @PreAuthorize("hasRole('PERSONAL')")
    public AsistenciaHoyRespuesta hoy() {
        return gestorAsistencia.obtenerHoy(personal());
    }

    /** Registra la entrada del día y devuelve el estado actualizado. */
    @PostMapping("/entrada")
    @PreAuthorize("hasRole('PERSONAL')")
    @ResponseStatus(HttpStatus.CREATED)
    public AsistenciaHoyRespuesta entrada() {
        return gestorAsistencia.registrarEntrada(personal());
    }

    /** Registra la salida; el motivo solo se usa cuando es anticipada. */
    @PostMapping("/salida")
    @PreAuthorize("hasRole('PERSONAL')")
    public AsistenciaHoyRespuesta salida(
            @Valid @RequestBody(required = false) SolicitudSalida solicitud) {
        String motivo = solicitud == null ? null : solicitud.motivo();
        return gestorAsistencia.registrarSalida(personal(), motivo);
    }

    private Estilista personal() {
        return (Estilista) usuarioActual.usuario();
    }
}
