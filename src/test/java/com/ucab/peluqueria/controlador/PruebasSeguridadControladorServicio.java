package com.ucab.peluqueria.controlador;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ucab.peluqueria.configuracion.ConfiguracionSeguridadMetodos;
import com.ucab.peluqueria.configuracion.ConfiguracionSeguridad;
import com.ucab.peluqueria.excepcion.ManejadorGlobalExcepciones;
import com.ucab.peluqueria.repositorio.RepositorioUsuario;
import com.ucab.peluqueria.seguridad.FiltroAutenticacionJwt;
import com.ucab.peluqueria.seguridad.ServicioJwt;
import com.ucab.peluqueria.servicio.GestorServicios;
import com.ucab.peluqueria.transferencia.ServicioRespuesta;

/** Pruebas de acceso al endpoint de servicios con la seguridad real configurada. */
@WebMvcTest(ControladorServicio.class)
@Import({
        ConfiguracionSeguridad.class,
        ConfiguracionSeguridadMetodos.class,
        ManejadorGlobalExcepciones.class,
        FiltroAutenticacionJwt.class,
        ServicioJwt.class
})
@TestPropertySource(properties = {
        "app.frontend-url=http://localhost:5173",
        "app.jwt.secret=secreto_de_prueba_para_tests_unitarios_123456789"
})
class PruebasSeguridadControladorServicio {

    private static final String CUERPO_VALIDO = """
            {"nombre":"Corte","precio":25.50,"duracionMinutos":45}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GestorServicios gestorServicios;

    @MockitoBean
    private RepositorioUsuario repositorioUsuario;

    @Test
    void sinSesionResponde401() throws Exception {
        mockMvc.perform(post("/api/servicios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_VALIDO))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void sinSesionPuedeListarServicios() throws Exception {
        when(gestorServicios.listar()).thenReturn(List.of(
                new ServicioRespuesta(1L, "Corte", new BigDecimal("25.50"), 45, null, null)));

        mockMvc.perform(get("/api/servicios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Corte"));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void unClienteAutenticadoResponde403() throws Exception {
        mockMvc.perform(post("/api/servicios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_VALIDO))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void unAdministradorPuedeCrearYResponde201() throws Exception {
        when(gestorServicios.crear(any())).thenReturn(
                new ServicioRespuesta(1L, "Corte", new BigDecimal("25.50"), 45, null, null));

        mockMvc.perform(post("/api/servicios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Corte"));
    }
}
