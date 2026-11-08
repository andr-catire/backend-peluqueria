package com.ucab.peluqueria.controlador;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ucab.peluqueria.modelo.Estilista;
import com.ucab.peluqueria.seguridad.UsuarioActual;
import com.ucab.peluqueria.servicio.GestorAsistencia;
import com.ucab.peluqueria.transferencia.AsistenciaHoyRespuesta;
import com.ucab.peluqueria.transferencia.SolicitudSalida;

import jakarta.validation.Valid;

/**
 * Autoservicio de asistencia del personal. El empleado sale de la sesión, nunca de la URL
 * ni del cuerpo de la petición.
 */
@RestController
@RequestMapping("/api/asistencia")
@PreAuthorize("hasRole('PERSONAL')")
public class ControladorAsistencia {

    private final GestorAsistencia gestorAsistencia;
    private final UsuarioActual usuarioActual;

    public ControladorAsistencia(GestorAsistencia gestorAsistencia, UsuarioActual usuarioActual) {
        this.gestorAsistencia = gestorAsistencia;
        this.usuarioActual = usuarioActual;
    }

    /** Estado de la jornada de hoy del empleado autenticado. */
    @GetMapping("/hoy")
    public AsistenciaHoyRespuesta hoy() {
        return gestorAsistencia.obtenerHoy(personal());
    }

    /** Registra la entrada del día y devuelve el estado actualizado. */
    @PostMapping("/entrada")
    @ResponseStatus(HttpStatus.CREATED)
    public AsistenciaHoyRespuesta entrada() {
        return gestorAsistencia.registrarEntrada(personal());
    }

    /** Registra la salida; el motivo solo se usa cuando es anticipada. */
    @PostMapping("/salida")
    public AsistenciaHoyRespuesta salida(
            @Valid @RequestBody(required = false) SolicitudSalida solicitud) {
        String motivo = solicitud == null ? null : solicitud.motivo();
        return gestorAsistencia.registrarSalida(personal(), motivo);
    }

    private Estilista personal() {
        return (Estilista) usuarioActual.usuario();
    }
}
