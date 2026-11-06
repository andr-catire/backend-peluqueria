package com.ucab.peluqueria.excepcion;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * Prueba el manejador sin levantar la aplicación ni tocar la base de datos:
 * un controlador falso lanza cada excepción y se revisa el estado HTTP y el JSON.
 */
class PruebasManejadorGlobalExcepciones {

    private MockMvc mockMvc;

    @BeforeEach
    void preparar() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ControladorFalso())
                .setControllerAdvice(new ManejadorGlobalExcepciones())
                .build();
    }

    @Test
    void recursoNoEncontradoResponde404() throws Exception {
        mockMvc.perform(get("/prueba/no-encontrado"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Servicio no encontrado"));
    }

    @Test
    void reglaDeNegocioUsaSuCodigoYSuEstado() throws Exception {
        mockMvc.perform(get("/prueba/regla"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("NOMBRE_REPETIDO"))
                .andExpect(jsonPath("$.message").value("Ya existe un servicio con ese nombre"));
    }

    @Test
    void validacionListaLosCampos() throws Exception {
        mockMvc.perform(post("/prueba/validar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("nombre: El nombre es obligatorio"));
    }

    @Test
    void jsonMalFormadoResponde400ConElFormatoDeLaApi() throws Exception {
        mockMvc.perform(post("/prueba/validar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{esto no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }

    @Test
    void metodoNoPermitidoResponde405NoUn500() throws Exception {
        mockMvc.perform(delete("/prueba/no-encontrado"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.error").value("METHOD_NOT_ALLOWED"));
    }

    @Test
    void citaSolapadaDeLaBaseDeDatosResponde409SlotTaken() throws Exception {
        mockMvc.perform(get("/prueba/solapada"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("SLOT_TAKEN"));
    }

    @Test
    void valorRepetidoDeLaBaseDeDatosResponde409Duplicate() throws Exception {
        mockMvc.perform(get("/prueba/duplicado"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("DUPLICATE"));
    }

    @Test
    void otraViolacionDeIntegridadResponde409Generico() throws Exception {
        mockMvc.perform(get("/prueba/integridad"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("DATA_CONFLICT"));
    }

    @Test
    void accesoDenegadoResponde403() throws Exception {
        mockMvc.perform(get("/prueba/prohibido"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    void errorInesperadoResponde500SinFiltrarElDetalle() throws Exception {
        mockMvc.perform(get("/prueba/inesperado"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("Ocurrió un error al procesar la solicitud."));
    }

    // ---------- Controlador falso, solo para las pruebas ----------

    record SolicitudDePrueba(@NotBlank(message = "El nombre es obligatorio") String nombre) {
    }

    @RestController
    @RequestMapping("/prueba")
    static class ControladorFalso {

        @GetMapping("/no-encontrado")
        void noEncontrado() {
            throw new ExcepcionRecursoNoEncontrado("Servicio no encontrado");
        }

        @GetMapping("/regla")
        void regla() {
            throw ExcepcionReglaNegocio.conflicto("NOMBRE_REPETIDO", "Ya existe un servicio con ese nombre");
        }

        @PostMapping("/validar")
        void validar(@Valid @RequestBody SolicitudDePrueba solicitud) {
        }

        @GetMapping("/solapada")
        void solapada() {
            throw new DataIntegrityViolationException("conflicto",
                    new SQLException("conflicting key value violates exclusion constraint", "23P01"));
        }

        @GetMapping("/duplicado")
        void duplicado() {
            throw new DataIntegrityViolationException("conflicto",
                    new SQLException("duplicate key value", "23505"));
        }

        @GetMapping("/integridad")
        void integridad() {
            throw new DataIntegrityViolationException("conflicto",
                    new SQLException("not null violation", "23502"));
        }

        @GetMapping("/prohibido")
        void prohibido() {
            throw new AccessDeniedException("sin permiso");
        }

        @GetMapping("/inesperado")
        void inesperado() {
            throw new IllegalStateException("detalle interno que no debe salir al cliente");
        }
    }
}
