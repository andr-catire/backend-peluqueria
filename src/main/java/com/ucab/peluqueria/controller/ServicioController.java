package com.ucab.peluqueria.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ucab.peluqueria.dto.ServicioRequest;
import com.ucab.peluqueria.dto.ServicioRespuesta;
import com.ucab.peluqueria.service.ServicioService;

import jakarta.validation.Valid;

/**
 * Endpoints REST de servicios. Recibe la petición, valida y delega en ServicioService.
 */
@RestController
@RequestMapping("/api/servicios")
public class ServicioController {

    private final ServicioService servicioService;

    public ServicioController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServicioRespuesta> crear(@Valid @RequestBody ServicioRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioService.crear(solicitud));
    }
}
