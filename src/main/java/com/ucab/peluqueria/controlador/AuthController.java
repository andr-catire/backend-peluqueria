package com.ucab.peluqueria.controlador;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ucab.peluqueria.servicio.AuthService;
import com.ucab.peluqueria.transferencia.RegistroRequest;
import com.ucab.peluqueria.transferencia.RegistroRespuesta;

import jakarta.validation.Valid;

/**
 * Registro público de cuentas. Es la única ruta de autenticación abierta; el inicio de sesión
 * y la emisión de JWT pertenecen a otra historia.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    public ResponseEntity<RegistroRespuesta> registrar(@Valid @RequestBody RegistroRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(solicitud));
    }
}
