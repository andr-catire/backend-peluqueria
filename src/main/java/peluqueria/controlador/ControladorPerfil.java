package peluqueria.controlador;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import peluqueria.servicio.GestorPerfil;
import peluqueria.transferencia.PerfilRespuesta;
import peluqueria.transferencia.SolicitudCambioPassword;
import peluqueria.transferencia.SolicitudPerfil;

import jakarta.validation.Valid;

/** Perfil del cliente autenticado: consulta, actualización de datos y cambio de contraseña. */
@RestController
@RequestMapping("/api/perfil")
@PreAuthorize("hasRole('CLIENTE')")
public class ControladorPerfil {

    private final GestorPerfil gestorPerfil;

    public ControladorPerfil(GestorPerfil gestorPerfil) {
        this.gestorPerfil = gestorPerfil;
    }

    @GetMapping
    public PerfilRespuesta obtener() {
        return gestorPerfil.obtener();
    }

    @PutMapping
    public PerfilRespuesta actualizar(@Valid @RequestBody SolicitudPerfil solicitud) {
        return gestorPerfil.actualizar(solicitud);
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarPassword(@Valid @RequestBody SolicitudCambioPassword solicitud) {
        gestorPerfil.cambiarPassword(solicitud);
    }
}
