package peluqueria.controlador;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

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

import peluqueria.configuracion.ConfiguracionSeguridad;
import peluqueria.configuracion.ConfiguracionSeguridadMetodos;
import peluqueria.excepcion.ManejadorGlobalExcepciones;
import peluqueria.repositorio.RepositorioUsuario;
import peluqueria.seguridad.FiltroAutenticacionJwt;
import peluqueria.seguridad.ServicioJwt;
import peluqueria.seguridad.UsuarioActual;
import peluqueria.servicio.GestorEstilistas;

/** Comprueba que el HTTP Basic opcional sigue funcionando sin convertirse en el login real. */
@WebMvcTest(ControladorEstilista.class)
@Import({
        ConfiguracionSeguridad.class,
        ConfiguracionSeguridadMetodos.class,
        ManejadorGlobalExcepciones.class,
        FiltroAutenticacionJwt.class,
        PruebasSeguridadBasic.UsuariosDePrueba.class
})
@TestPropertySource(properties = {
        "app.frontend-url=http://localhost:5173",
        "app.basic-auth=true"
})
class PruebasSeguridadBasic {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GestorEstilistas gestorEstilistas;

    @MockitoBean
    private UsuarioActual usuarioActual;

    @MockitoBean
    private ServicioJwt servicioJwt;

    @MockitoBean
    private RepositorioUsuario repositorioUsuario;

    @Test
    void sinCredencialesResponde401() throws Exception {
        mockMvc.perform(get("/api/personal"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void conCredencialesInvalidasResponde401() throws Exception {
        mockMvc.perform(get("/api/personal")
                        .with(httpBasic("admin@prueba.com", "incorrecta")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void elAdministradorSeAutenticaConBasicYAccede() throws Exception {
        when(gestorEstilistas.listar()).thenReturn(List.of());

        mockMvc.perform(get("/api/personal")
                        .with(httpBasic("admin@prueba.com", "secreta")))
                .andExpect(status().isOk());
    }

    @Test
    void unClienteAutenticadoConBasicNoPuedeAccederAlAreaAdmin() throws Exception {
        mockMvc.perform(get("/api/personal")
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
