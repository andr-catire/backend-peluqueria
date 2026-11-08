package com.ucab.peluqueria.controlador;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ucab.peluqueria.modelo.Rol;
import com.ucab.peluqueria.seguridad.UsuarioActual;
import com.ucab.peluqueria.servicio.CitaService;
import com.ucab.peluqueria.transferencia.CitaRespuesta;
import com.ucab.peluqueria.transferencia.EstilistaResumenRespuesta;
import com.ucab.peluqueria.transferencia.SolicitudAgendarCita;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class ControladorCita {

    private final CitaService citaService;
    private final UsuarioActual usuarioActual;

    public ControladorCita(CitaService citaService, UsuarioActual usuarioActual) {
        this.citaService = citaService;
        this.usuarioActual = usuarioActual;
    }

    /** Lista los estilistas que ofrecen un servicio dado. */
    @GetMapping("/servicios/{servicioId}/estilistas")
    public List<EstilistaResumenRespuesta> obtenerEstilistas(@PathVariable Long servicioId) {
        return citaService.obtenerEstilistasPorServicio(servicioId);
    }

    /** Consulta las horas disponibles de un estilista en una fecha para un servicio. */
    @GetMapping("/citas/disponibilidad")
    public List<String> obtenerDisponibilidad(
            @RequestParam Long servicioId,
            @RequestParam Long estilistaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        Long clienteId = null;
        try {
            clienteId = usuarioActual.id();
        } catch (Exception ignored) {
            // Usuario no autenticado todavía consultando horas
        }
        return citaService.obtenerDisponibilidad(servicioId, estilistaId, fecha, clienteId);
    }

    /** Agenda una nueva cita (cliente autenticado o admin). */
    @PostMapping("/citas")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public ResponseEntity<CitaRespuesta> agendar(@Valid @RequestBody SolicitudAgendarCita solicitud) {
        Long clienteId = usuarioActual.id();
        CitaRespuesta respuesta = citaService.agendar(solicitud, clienteId);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /** Obtiene el historial de citas del cliente autenticado. */
    @GetMapping("/citas/mis-citas")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public List<CitaRespuesta> obtenerMisCitas() {
        return citaService.obtenerMisCitas(usuarioActual.id());
    }

    /** Obtiene la agenda semanal para administradores o estilistas. */
    @GetMapping("/citas/agenda-semanal")
    @PreAuthorize("hasAnyRole('ADMIN', 'PERSONAL')")
    public List<CitaRespuesta> obtenerAgendaSemanal(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) Long estilistaId) {
        var usuario = usuarioActual.usuario();
        if (usuario.getRol() == Rol.PERSONAL) {
            estilistaId = usuario.getId();
        }
        return citaService.obtenerAgendaSemanal(fechaInicio, fechaFin, estilistaId);
    }
}
