package peluqueria.controlador;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import peluqueria.excepcion.ManejadorGlobalExcepciones;
import peluqueria.modelo.Rol;
import peluqueria.modelo.Usuario;
import peluqueria.repositorio.RepositorioUsuario;
import peluqueria.seguridad.ServicioJwt;
import peluqueria.servicio.AuthService;

class PruebasControladorAuth {

    private RepositorioUsuario usuarioRepository;
    private PasswordEncoder passwordEncoder;
    private ServicioJwt servicioJwt;
    private AuthService authService;
    private MockMvc mockMvc;

    @BeforeEach
    void preparar() {
        usuarioRepository = mock(RepositorioUsuario.class);
        passwordEncoder = mock(PasswordEncoder.class);
        servicioJwt = mock(ServicioJwt.class);
        authService = mock(AuthService.class);

        ControladorAuth controlador = new ControladorAuth(
                usuarioRepository, passwordEncoder, servicioJwt, authService);

        mockMvc = MockMvcBuilders.standaloneSetup(controlador)
                .setControllerAdvice(new ManejadorGlobalExcepciones())
                .build();
    }

    @Test
    void loginExitosoDevuelveTokenYDatosDelUsuario() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Administrador");
        usuario.setEmail("admin@peluqueria.com");
        usuario.setPassword("hash_bcrypt");
        usuario.setRol(Rol.ADMIN);
        usuario.setActivo(true);

        when(usuarioRepository.findByEmailIgnoreCase("admin@peluqueria.com"))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Admin1234*", "hash_bcrypt")).thenReturn(true);
        when(servicioJwt.generarToken(any())).thenReturn("token.jwt.firmado");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@peluqueria.com","password":"Admin1234*"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token.jwt.firmado"))
                .andExpect(jsonPath("$.rol").value("ADMIN"))
                .andExpect(jsonPath("$.nombre").value("Administrador"))
                .andExpect(jsonPath("$.email").value("admin@peluqueria.com"));
    }

    @Test
    void loginConCredencialesIncorrectasResponde401() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setEmail("admin@peluqueria.com");
        usuario.setPassword("hash_bcrypt");
        usuario.setActivo(true);

        when(usuarioRepository.findByEmailIgnoreCase("admin@peluqueria.com"))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("clave_mala", "hash_bcrypt")).thenReturn(false);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@peluqueria.com","password":"clave_mala"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void loginConUsuarioInexistenteResponde401() throws Exception {
        when(usuarioRepository.findByEmailIgnoreCase("noexiste@peluqueria.com"))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"noexiste@peluqueria.com","password":"Admin1234*"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void loginConUsuarioInactivoResponde401() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setEmail("inactivo@peluqueria.com");
        usuario.setPassword("hash_bcrypt");
        usuario.setActivo(false);

        when(usuarioRepository.findByEmailIgnoreCase("inactivo@peluqueria.com"))
                .thenReturn(Optional.of(usuario));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"inactivo@peluqueria.com","password":"Admin1234*"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }
}
