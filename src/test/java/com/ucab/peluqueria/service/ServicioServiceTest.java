package com.ucab.peluqueria.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;

import com.ucab.peluqueria.dto.ServicioRequest;
import com.ucab.peluqueria.dto.ServicioRespuesta;
import com.ucab.peluqueria.exception.ReglaNegocioException;
import com.ucab.peluqueria.model.Servicio;
import com.ucab.peluqueria.repository.ServicioRepository;

/** Pruebas de las reglas de negocio de servicios. Usa un repositorio simulado. */
class ServicioServiceTest {

    private ServicioRepository repositorio;
    private ServicioService servicio;

    @BeforeEach
    void preparar() {
        repositorio = mock(ServicioRepository.class);
        servicio = new ServicioService(repositorio);
    }

    private static ServicioRequest solicitud(String nombre, String costo, String duracion) {
        return new ServicioRequest(nombre, new BigDecimal(costo), new BigDecimal(duracion));
    }

    @Test
    void creaNormalizaElNombreYGuardaActivo() {
        when(repositorio.existsByNombreIgnoreCaseAndActivoTrue("Corte de cabello")).thenReturn(false);
        when(repositorio.save(any(Servicio.class))).thenAnswer(invocacion -> {
            Servicio guardado = invocacion.getArgument(0);
            guardado.setId(7L);
            return guardado;
        });

        ServicioRespuesta respuesta = servicio.crear(solicitud("  Corte   de cabello  ", "25.50", "45"));

        ArgumentCaptor<Servicio> captor = ArgumentCaptor.forClass(Servicio.class);
        verify(repositorio).save(captor.capture());
        Servicio guardado = captor.getValue();

        assertEquals("Corte de cabello", guardado.getNombre());
        assertEquals(0, new BigDecimal("25.50").compareTo(guardado.getCosto()));
        assertEquals(45, guardado.getDuracionMinutos());
        assertTrue(guardado.isActivo());
        assertEquals(7L, respuesta.id());
        assertEquals("Corte de cabello", respuesta.nombre());
    }

    @Test
    void rechazaUnNombreDuplicadoSinGuardar() {
        when(repositorio.existsByNombreIgnoreCaseAndActivoTrue("Corte de cabello")).thenReturn(true);

        ReglaNegocioException excepcion = assertThrows(
                ReglaNegocioException.class,
                () -> servicio.crear(solicitud("Corte de cabello", "25.50", "45")));

        assertEquals("DUPLICATE", excepcion.getCodigo());
        assertEquals(HttpStatus.CONFLICT, excepcion.getStatus());
        verify(repositorio, never()).save(any());
    }
}
