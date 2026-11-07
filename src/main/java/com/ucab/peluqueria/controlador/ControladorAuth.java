package com.ucab.peluqueria.controlador;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ucab.peluqueria.modelo.Usuario;
import com.ucab.peluqueria.repositorio.RepositorioUsuario;
import com.ucab.peluqueria.seguridad.ServicioJwt;
import com.ucab.peluqueria.transferencia.ErrorRespuesta;
import com.ucab.peluqueria.transferencia.RespuestaLogin;
import com.ucab.peluqueria.transferencia.SolicitudLogin;

import jakarta.validation.Valid;

/**
 * Controlador de autenticación. Permite a los usuarios identificarse y obtener un token JWT.
 */
@RestController
@RequestMapping("/api/auth")
public class ControladorAuth {

    private final RepositorioUsuario usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ServicioJwt servicioJwt;

    public ControladorAuth(
            RepositorioUsuario usuarioRepository,
            PasswordEncoder passwordEncoder,
            ServicioJwt servicioJwt) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.servicioJwt = servicioJwt;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody SolicitudLogin solicitud) {
        String correo = solicitud.email() == null ? "" : solicitud.email().trim();

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailIgnoreCase(correo);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorRespuesta("UNAUTHORIZED", "Credenciales incorrectas. Intenta de nuevo."));
        }

        Usuario usuario = usuarioOpt.get();

        if (!usuario.isActivo()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorRespuesta("UNAUTHORIZED", "La cuenta se encuentra inactiva."));
        }

        if (!passwordEncoder.matches(solicitud.password(), usuario.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorRespuesta("UNAUTHORIZED", "Credenciales incorrectas. Intenta de nuevo."));
        }

        String token = servicioJwt.generarToken(usuario);

        RespuestaLogin respuesta = new RespuestaLogin(
                token,
                usuario.getRol().name(),
                usuario.getNombre(),
                usuario.getEmail()
        );

        return ResponseEntity.ok(respuesta);
    }
}
