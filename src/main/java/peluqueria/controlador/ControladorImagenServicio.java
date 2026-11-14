package peluqueria.controlador;

import java.time.Duration;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import peluqueria.modelo.ImagenServicio;
import peluqueria.servicio.GestorImagenes;

/** Sirve la imagen pública de un servicio. La usa el catálogo del cliente y del panel. */
@RestController
@RequestMapping("/api/servicios")
public class ControladorImagenServicio {

    private final GestorImagenes gestorImagenes;

    public ControladorImagenServicio(GestorImagenes gestorImagenes) {
        this.gestorImagenes = gestorImagenes;
    }

    @GetMapping("/{idServicio}/imagen")
    public ResponseEntity<byte[]> imagen(@PathVariable Long idServicio) {
        ImagenServicio imagen = gestorImagenes.obtener(idServicio);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(imagen.getTipo()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(1)).cachePublic())
                .body(imagen.getDatos());
    }
}
