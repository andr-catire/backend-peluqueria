package com.ucab.peluqueria.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;

import com.ucab.peluqueria.excepcion.ExcepcionReglaNegocio;
import com.ucab.peluqueria.modelo.Servicio;
import com.ucab.peluqueria.repositorio.RepositorioServicio;
import com.ucab.peluqueria.transferencia.ServicioRespuesta;
import com.ucab.peluqueria.transferencia.SolicitudServicio;

/** Pruebas de las reglas de negocio de servicios. Usa un repositorio simulado. */
class PruebasGestorServicios {

    private RepositorioServicio repositorio;
    private GestorServicios servicio;

    @BeforeEach
    void preparar() {
        repositorio = mock(RepositorioServicio.class);
        servicio = new GestorServicios(repositorio);
    }

    private static SolicitudServicio solicitud(String nombre, String costo, String duracion) {
        return new SolicitudServicio(nombre, new BigDecimal(costo), new BigDecimal(duracion), "Descripción");
    }

    @Test
    void creaNormalizaElNombreYGuardaActivo() {
        when(repositorio.findByNombreIgnoreCase("Corte de cabello")).thenReturn(Optional.empty());
        when(repositorio.save(any(Servicio.class))).thenAnswer(invocacion -> {
            Servicio guardado = invocacion.getArgument(0);
            guardado.setId(7L);
            return guardado;
        });

        ServicioRespuesta respuesta =
                servicio.crear(solicitud("  Corte   de cabello  ", "25.50", "45"));

        ArgumentCaptor<Servicio> captor = ArgumentCaptor.forClass(Servicio.class);
        verify(repositorio).save(captor.capture());
        Servicio guardado = captor.getValue();

        assertEquals("Corte de cabello", guardado.getNombre());
        assertEquals("Descripción", guardado.getDescripcion());
        assertEquals(0, new BigDecimal("25.50").compareTo(guardado.getCosto()));
        assertEquals(45, guardado.getDuracionMinutos());
        assertTrue(guardado.isActivo());
        assertEquals(7L, respuesta.id());
        assertEquals("Corte de cabello", respuesta.nombre());
        assertEquals("Descripción", respuesta.descripcion());
    }

    @Test
    void rechazaUnServicioActivoDuplicadoSinGuardar() {
        Servicio existente = new Servicio();
        existente.setId(1L);
        existente.setNombre("Corte de cabello");
        existente.setActivo(true);
        when(repositorio.findByNombreIgnoreCase("Corte de cabello")).thenReturn(Optional.of(existente));

        ExcepcionReglaNegocio excepcion = assertThrows(
                ExcepcionReglaNegocio.class,
                () -> servicio.crear(solicitud("Corte de cabello", "25.50", "45")));

        assertEquals("DUPLICATE", excepcion.getCodigo());
        assertEquals("El servicio ya existe.", excepcion.getMessage());
        assertEquals(HttpStatus.CONFLICT, excepcion.getStatus());
        verify(repositorio, never()).save(any());
    }

    @Test
    void reactivaUnServicioInactivoYActualizaSusDatos() {
        Servicio existente = new Servicio();
        existente.setId(2L);
        existente.setNombre("Corte de cabello");
        existente.setDescripcion("Anterior");
        existente.setCosto(new BigDecimal("10.00"));
        existente.setDuracionMinutos(30);
        existente.setActivo(false);
        when(repositorio.findByNombreIgnoreCase("Corte de cabello")).thenReturn(Optional.of(existente));
        when(repositorio.save(any(Servicio.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        ServicioRespuesta respuesta =
                servicio.crear(solicitud("Corte de cabello", "25.50", "45"));

        assertTrue(existente.isActivo());
        assertEquals("Descripción", existente.getDescripcion());
        assertEquals(0, new BigDecimal("25.50").compareTo(existente.getCosto()));
        assertEquals(45, existente.getDuracionMinutos());
        assertEquals(2L, respuesta.id());
        verify(repositorio).save(existente);
    }
}
