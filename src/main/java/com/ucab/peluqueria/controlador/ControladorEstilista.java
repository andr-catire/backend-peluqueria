package com.ucab.peluqueria.controlador;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ucab.peluqueria.seguridad.UsuarioActual;
import com.ucab.peluqueria.servicio.GestorEstilistas;
import com.ucab.peluqueria.transferencia.EstilistaRequest;
import com.ucab.peluqueria.transferencia.EstilistaRespuesta;

import jakarta.validation.Valid;

/**
 * Endpoints del personal. El id del empleado autenticado sale del contexto de seguridad,
 * nunca de la URL. Lo usa el panel de administración.
 */
@RestController
@RequestMapping("/api/personal")
public class ControladorEstilista {

    private final GestorEstilistas gestorEstilistas;
    private final UsuarioActual usuarioActual;

    public ControladorEstilista(GestorEstilistas gestorEstilistas, UsuarioActual usuarioActual) {
        this.gestorEstilistas = gestorEstilistas;
        this.usuarioActual = usuarioActual;
    }

    /** Crea una cuenta de estilista. Solo ADMIN. */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EstilistaRespuesta> crear(@Valid @RequestBody EstilistaRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(gestorEstilistas.crear(solicitud, usuarioActual.email()));
    }

    /** Lista los estilistas activos. Solo ADMIN. */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<EstilistaRespuesta> listar() {
        return gestorEstilistas.listar();
    }

    /** Perfil del estilista autenticado. Solo PERSONAL. */
    @GetMapping("/mi-perfil")
    @PreAuthorize("hasRole('PERSONAL')")
    public EstilistaRespuesta miPerfil() {
        return gestorEstilistas.obtenerMiPerfil(usuarioActual.id());
    }

    /** Desactiva un estilista. Solo ADMIN. */
    @DeleteMapping("/{idEstilista}/desvincular")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desvincular(@PathVariable Long idEstilista) {
        gestorEstilistas.desvincularEstilista(idEstilista, usuarioActual.email());
        return ResponseEntity.noContent().build();
    }
}
