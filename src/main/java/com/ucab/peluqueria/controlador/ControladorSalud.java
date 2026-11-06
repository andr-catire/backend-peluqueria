package com.ucab.peluqueria.controlador;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Comprobación de vida del backend (la usa el frontend y el despliegue). */
@RestController
@RequestMapping("/api/health")
public class ControladorSalud {

    @GetMapping
    public Map<String, String> estado() {
        return Map.of("status", "ok");
    }
}
