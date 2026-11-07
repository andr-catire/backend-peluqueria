package com.ucab.peluqueria.controlador;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ucab.peluqueria.configuracion.ConfiguracionSeguridad;
import com.ucab.peluqueria.configuracion.ConfiguracionSeguridadMetodos;
import com.ucab.peluqueria.excepcion.ManejadorGlobalExcepciones;
import com.ucab.peluqueria.modelo.Rol;
import com.ucab.peluqueria.servicio.AuthService;
import com.ucab.peluqueria.transferencia.RegistroRespuesta;

/** El registro público debe funcionar sin token y validar los datos de entrada. */
@WebMvcTest(AuthController.class)
@Import({ConfiguracionSeguridad.class, ConfiguracionSeguridadMetodos.class, ManejadorGlobalExcepciones.class})
@TestPropertySource(properties = "app.frontend-url=http://localhost:5173")
class PruebasSeguridadAuthController {

    private static final String CUERPO_VALIDO = """
            {
              "nombre":"Ana Pérez",
              "email":"ana@correo.com",
              "password":"secreta123",
              "cedula":"V-12345678",
              "telefono":"0414-1234567"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void elRegistroEsPublicoYResponde201() throws Exception {
        when(authService.registrar(any()))
                .thenReturn(new RegistroRespuesta(15L, "Ana Pérez", "ana@correo.com", Rol.CLIENTE));

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(15))
                .andExpect(jsonPath("$.email").value("ana@correo.com"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"));
    }

    @Test
    void rechazaUnRegistroConContrasenaCorta() throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre":"Ana Pérez",
                                  "email":"ana@correo.com",
                                  "password":"corta",
                                  "cedula":"V-12345678",
                                  "telefono":"0414-1234567"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
}
