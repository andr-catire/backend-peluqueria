package com.ucab.peluqueria.controlador;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.ucab.peluqueria.transferencia.ServicioRespuesta;
import com.ucab.peluqueria.excepcion.ManejadorGlobalExcepciones;
import com.ucab.peluqueria.excepcion.ExcepcionReglaNegocio;
import com.ucab.peluqueria.servicio.GestorServicios;

/** Pruebas HTTP del controlador de servicios, sin base de datos ni seguridad real. */
class PruebasControladorServicio {

    private GestorServicios gestorServicios;
    private MockMvc mockMvc;

    @BeforeEach
    void preparar() {
        gestorServicios = mock(GestorServicios.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ControladorServicio(gestorServicios))
                .setControllerAdvice(new ManejadorGlobalExcepciones())
                .build();
    }

    @Test
    void creaUnServicioValidoYResponde201() throws Exception {
        when(gestorServicios.crear(any())).thenReturn(
                new ServicioRespuesta(1L, "Corte de cabello", new BigDecimal("25.50"), 45));

        mockMvc.perform(post("/api/servicios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Corte de cabello","costo":25.50,"duracionMinutos":45}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Corte de cabello"))
                .andExpect(jsonPath("$.costo").value(25.50))
                .andExpect(jsonPath("$.duracionMinutos").value(45));
    }

    @ParameterizedTest
    @MethodSource("cuerposInvalidos")
    void rechazaDatosInvalidosCon400(String cuerpo) throws Exception {
        mockMvc.perform(post("/api/servicios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    static Stream<String> cuerposInvalidos() {
        return Stream.of(
                """
                {"nombre":"","costo":25.50,"duracionMinutos":45}
                """,
                """
                {"nombre":"Corte","costo":0,"duracionMinutos":45}
                """,
                """
                {"nombre":"Corte","costo":-5,"duracionMinutos":45}
                """,
                """
                {"nombre":"Corte","costo":25.50,"duracionMinutos":0}
                """,
                """
                {"nombre":"Corte","costo":25.50,"duracionMinutos":45.5}
                """);
    }

    @Test
    void responde409CuandoElServicioLanzaDuplicado() throws Exception {
        when(gestorServicios.crear(any())).thenThrow(
                ExcepcionReglaNegocio.conflicto("DUPLICATE", "Ya existe un servicio con ese nombre."));

        mockMvc.perform(post("/api/servicios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Corte","costo":25.50,"duracionMinutos":45}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("DUPLICATE"))
                .andExpect(jsonPath("$.message").value("Ya existe un servicio con ese nombre."));
    }
}
