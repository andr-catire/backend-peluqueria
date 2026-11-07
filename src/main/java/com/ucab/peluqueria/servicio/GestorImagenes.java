package com.ucab.peluqueria.servicio;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ucab.peluqueria.excepcion.ExcepcionRecursoNoEncontrado;
import com.ucab.peluqueria.excepcion.ExcepcionReglaNegocio;
import com.ucab.peluqueria.modelo.ImagenServicio;
import com.ucab.peluqueria.repositorio.RepositorioImagenServicio;

/**
 * Valida y guarda la imagen de un servicio. La llama GestorServicios; usa RepositorioImagenServicio.
 * El frontend envía WebP; aquí se conserva el formato recibido para no recomprimir.
 */
@Service
public class GestorImagenes {

    static final long MAX_BYTES = 10L * 1024 * 1024;

    private final RepositorioImagenServicio repositorioImagen;

    public GestorImagenes(RepositorioImagenServicio repositorioImagen) {
        this.repositorioImagen = repositorioImagen;
    }

    @Transactional
    public void guardar(Long idServicio, MultipartFile archivo) throws IOException {
        if (archivo == null || archivo.isEmpty()) {
            return;
        }
        if (archivo.getSize() > MAX_BYTES) {
            throw ExcepcionReglaNegocio.invalida(
                    "FILE_TOO_LARGE", "La imagen no puede superar los 10 MB.");
        }

        byte[] datos = archivo.getBytes();
        String tipo = detectarTipo(datos);
        if (tipo == null) {
            throw ExcepcionReglaNegocio.invalida(
                    "INVALID_IMAGE", "Solo se permiten imágenes JPG, PNG o WEBP válidas.");
        }

        ImagenServicio imagen = new ImagenServicio();
        imagen.setId(idServicio);
        imagen.setTipo(tipo);
        imagen.setDatos(datos);
        repositorioImagen.save(imagen);
    }

    @Transactional(readOnly = true)
    public ImagenServicio obtener(Long idServicio) {
        return repositorioImagen.findById(idServicio)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado(
                        "El servicio no tiene imagen."));
    }

    private static String detectarTipo(byte[] datos) {
        if (datos.length >= 8
                && (datos[0] & 0xff) == 0x89
                && datos[1] == 'P' && datos[2] == 'N' && datos[3] == 'G'
                && (datos[4] & 0xff) == 0x0d && (datos[5] & 0xff) == 0x0a
                && datos[6] == 0x1a && datos[7] == 0x0a) {
            return "image/png";
        }
        if (datos.length >= 3
                && (datos[0] & 0xff) == 0xff
                && (datos[1] & 0xff) == 0xd8
                && (datos[2] & 0xff) == 0xff) {
            return "image/jpeg";
        }
        if (datos.length >= 12
                && "RIFF".equals(new String(datos, 0, 4, StandardCharsets.US_ASCII))
                && "WEBP".equals(new String(datos, 8, 4, StandardCharsets.US_ASCII))) {
            return "image/webp";
        }
        return null;
    }
}
