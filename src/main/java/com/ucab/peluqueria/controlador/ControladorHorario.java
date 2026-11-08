package com.ucab.peluqueria.controlador;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ucab.peluqueria.configuracion.PropiedadesHorario;
import com.ucab.peluqueria.seguridad.UsuarioActual;
import com.ucab.peluqueria.servicio.GestorHorario;
import com.ucab.peluqueria.transferencia.HorarioRespuesta;
import com.ucab.peluqueria.transferencia.SolicitudHorario;

import jakarta.validation.Valid;

/**
 * Horario del local para el panel del administrador. Cambiarlo afecta de inmediato
 * las reglas de entrada y salida del personal.
 */
@RestController
@RequestMapping("/api/asistencia/horario")
@PreAuthorize("hasRole('ADMIN')")
public class ControladorHorario {

    private final GestorHorario gestorHorario;
    private final UsuarioActual usuarioActual;

    public ControladorHorario(GestorHorario gestorHorario, UsuarioActual usuarioActual) {
        this.gestorHorario = gestorHorario;
        this.usuarioActual = usuarioActual;
    }

    /** Horario vigente. */
    @GetMapping
    public HorarioRespuesta obtener() {
        return HorarioRespuesta.desde(gestorHorario.actual());
    }

    /** Actualiza apertura y cierre. */
    @PutMapping
    public HorarioRespuesta actualizar(@Valid @RequestBody SolicitudHorario solicitud) {
        PropiedadesHorario horario = gestorHorario.actualizar(
                solicitud.apertura(), solicitud.cierre(), usuarioActual.email());
        return HorarioRespuesta.desde(horario);
    }
}
