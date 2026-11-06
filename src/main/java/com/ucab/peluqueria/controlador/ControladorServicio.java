package com.ucab.peluqueria.controlador;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ucab.peluqueria.transferencia.SolicitudServicio;
import com.ucab.peluqueria.transferencia.ServicioRespuesta;
import com.ucab.peluqueria.servicio.GestorServicios;

import jakarta.validation.Valid;

/**
 * Endpoints REST de servicios. Recibe la petición, valida y delega en GestorServicios.
 */
@RestController
@RequestMapping("/api/servicios")
public class ControladorServicio {

    private final GestorServicios gestorServicios;

    public ControladorServicio(GestorServicios gestorServicios) {
        this.gestorServicios = gestorServicios;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServicioRespuesta> crear(@Valid @RequestBody SolicitudServicio solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gestorServicios.crear(solicitud));
    }
}
