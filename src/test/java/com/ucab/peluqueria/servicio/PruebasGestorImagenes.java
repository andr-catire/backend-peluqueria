package com.ucab.peluqueria.servicio;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import com.ucab.peluqueria.modelo.ImagenServicio;
import com.ucab.peluqueria.repositorio.RepositorioImagenServicio;

/** Prueba que la imagen WebP de un servicio se guarde con el id correcto. */
class PruebasGestorImagenes {

    @Test
    void guardaUnWebpValidoConElIdDelServicio() throws Exception {
        RepositorioImagenServicio repositorio = mock(RepositorioImagenServicio.class);
        GestorImagenes gestor = new GestorImagenes(repositorio);
        byte[] datos = new byte[]{
                'R', 'I', 'F', 'F', 0, 0, 0, 0,
                'W', 'E', 'B', 'P', 1, 2, 3
        };
        MockMultipartFile archivo = new MockMultipartFile(
                "imagen", "servicio.webp", "image/webp", datos);

        gestor.guardar(7L, archivo);

        ArgumentCaptor<ImagenServicio> captor = ArgumentCaptor.forClass(ImagenServicio.class);
        verify(repositorio).save(captor.capture());
        ImagenServicio imagen = captor.getValue();

        assertEquals(7L, imagen.getId());
        assertEquals("image/webp", imagen.getTipo());
        assertArrayEquals(datos, imagen.getDatos());
    }
}
