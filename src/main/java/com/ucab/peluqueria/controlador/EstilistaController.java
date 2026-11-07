package com.ucab.peluqueria.controlador;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ucab.peluqueria.seguridad.UsuarioActual;
import com.ucab.peluqueria.servicio.EstilistaService;
import com.ucab.peluqueria.transferencia.EstilistaRequest;
import com.ucab.peluqueria.transferencia.EstilistaRespuesta;
import com.ucab.peluqueria.transferencia.UsuarioBusquedaRespuesta;

import jakarta.validation.Valid;

/**
 * Endpoints de gestión del personal. El id del empleado autenticado sale del contexto de
 * seguridad, nunca de la URL.
 */
@RestController
@RequestMapping("/api/personal")
public class EstilistaController {

    private final EstilistaService estilistaService;
    private final UsuarioActual usuarioActual;

    public EstilistaController(EstilistaService estilistaService, UsuarioActual usuarioActual) {
        this.estilistaService = estilistaService;
        this.usuarioActual = usuarioActual;
    }

    @GetMapping("/buscar")
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioBusquedaRespuesta buscarPorEmail(@RequestParam String email) {
        return estilistaService.buscarPorEmail(email);
    }

    @PostMapping("/promover")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EstilistaRespuesta> promover(@Valid @RequestBody EstilistaRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(estilistaService.promoverAEmpleado(solicitud));
    }

    @GetMapping("/mi-perfil")
    @PreAuthorize("hasRole('PERSONAL')")
    public EstilistaRespuesta miPerfil() {
        return estilistaService.obtenerMiPerfil(usuarioActual.id());
    }

    @DeleteMapping("/{idEstilista}/desvincular")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desvincular(@PathVariable Long idEstilista) {
        estilistaService.desvincularEstilista(idEstilista);
        return ResponseEntity.noContent().build();
    }
}
