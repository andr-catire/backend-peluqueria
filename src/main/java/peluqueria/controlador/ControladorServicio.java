package peluqueria.controlador;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import peluqueria.servicio.GestorServicios;
import peluqueria.transferencia.ServicioRespuesta;
import peluqueria.transferencia.SolicitudServicio;

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

    /** Crea un servicio sin imagen. Solo ADMIN. */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServicioRespuesta> crear(@Valid @RequestBody SolicitudServicio solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gestorServicios.crear(solicitud));
    }

    /** Crea un servicio con imagen opcional. Solo ADMIN. */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServicioRespuesta> crearConImagen(
            @Valid @RequestPart("servicio") SolicitudServicio solicitud,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(gestorServicios.crear(solicitud, imagen));
    }

    /** Lista los servicios activos. Es público: no lleva @PreAuthorize. */
    @GetMapping
    public List<ServicioRespuesta> listar() {
        return gestorServicios.listar();
    }

    /** Devuelve un servicio activo por id. Es público: no lleva @PreAuthorize. */
    @GetMapping("/{id}")
    public ServicioRespuesta obtener(@PathVariable Long id) {
        return gestorServicios.obtener(id);
    }
}
