package com.ucab.peluqueria.controlador;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ucab.peluqueria.configuracion.ConfiguracionSeguridad;
import com.ucab.peluqueria.configuracion.ConfiguracionSeguridadMetodos;
import com.ucab.peluqueria.excepcion.ManejadorGlobalExcepciones;
import com.ucab.peluqueria.modelo.Rol;
import com.ucab.peluqueria.repositorio.RepositorioUsuario;
import com.ucab.peluqueria.seguridad.FiltroAutenticacionJwt;
import com.ucab.peluqueria.seguridad.ServicioJwt;
import com.ucab.peluqueria.seguridad.UsuarioActual;
import com.ucab.peluqueria.servicio.EstilistaService;
import com.ucab.peluqueria.transferencia.UsuarioBusquedaRespuesta;

/**
 * Comprueba que el HTTP Basic opcional (app.basic-auth=true) autentica y aplica los roles.
 * Es el mecanismo pensado para probar por curl o Postman mientras no exista el login con JWT.
 */
@WebMvcTest(EstilistaController.class)
@Import({
        ConfiguracionSeguridad.class,
        ConfiguracionSeguridadMetodos.class,
        ManejadorGlobalExcepciones.class,
        FiltroAutenticacionJwt.class,
        ServicioJwt.class,
        PruebasSeguridadBasic.UsuariosDePrueba.class
})
@TestPropertySource(properties = {
        "app.frontend-url=http://localhost:5173",
        "app.basic-auth=true",
        "app.jwt.secret=secreto_de_prueba_para_tests_unitarios_123456789"
})
class PruebasSeguridadBasic {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EstilistaService estilistaService;

    @MockitoBean
    private UsuarioActual usuarioActual;

    @MockitoBean
    private RepositorioUsuario repositorioUsuario;

    @Test
    void sinCredencialesResponde401() throws Exception {
        mockMvc.perform(get("/api/personal/buscar").param("email", "ana@correo.com"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void conCredencialesInvalidasResponde401() throws Exception {
        mockMvc.perform(get("/api/personal/buscar")
                        .param("email", "ana@correo.com")
                        .with(httpBasic("admin@prueba.com", "incorrecta")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void elAdministradorSeAutenticaConBasicYAccede() throws Exception {
        when(estilistaService.buscarPorEmail("ana@correo.com"))
                .thenReturn(new UsuarioBusquedaRespuesta(3L, "Ana", "ana@correo.com", Rol.CLIENTE));

        mockMvc.perform(get("/api/personal/buscar")
                        .param("email", "ana@correo.com")
                        .with(httpBasic("admin@prueba.com", "secreta")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    void unClienteAutenticadoConBasicNoPuedeAccederAlAreaAdmin() throws Exception {
        mockMvc.perform(get("/api/personal/buscar")
                        .param("email", "ana@correo.com")
                        .with(httpBasic("cliente@prueba.com", "secreta")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @TestConfiguration
    static class UsuariosDePrueba {

        @Bean
        UserDetailsService usuariosDePrueba(PasswordEncoder passwordEncoder) {
            return new InMemoryUserDetailsManager(
                    User.withUsername("admin@prueba.com")
                            .password(passwordEncoder.encode("secreta"))
                            .roles("ADMIN")
                            .build(),
                    User.withUsername("cliente@prueba.com")
                            .password(passwordEncoder.encode("secreta"))
                            .roles("CLIENTE")
                            .build());
        }
    }
}
