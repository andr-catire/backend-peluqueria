package peluqueria.seguridad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import peluqueria.modelo.Rol;
import peluqueria.modelo.Usuario;
import peluqueria.repositorio.RepositorioUsuario;

/** Pruebas del filtro JWT: un token válido no autentica a una cuenta desactivada. */
class PruebasFiltroAutenticacionJwt {

    private final ServicioJwt servicioJwt = mock(ServicioJwt.class);
    private final RepositorioUsuario repositorioUsuario = mock(RepositorioUsuario.class);
    private final FiltroAutenticacionJwt filtro =
            new FiltroAutenticacionJwt(servicioJwt, repositorioUsuario);

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    private static Usuario usuario(boolean activo) {
        Usuario usuario = new Usuario();
        usuario.setEmail("persona@correo.com");
        usuario.setRol(Rol.PERSONAL);
        usuario.setActivo(activo);
        return usuario;
    }

    @Test
    void noAutenticaUnaCuentaDesactivadaAunqueElTokenSeaValido() throws Exception {
        when(servicioJwt.validarToken("token")).thenReturn(true);
        when(servicioJwt.extraerEmail("token")).thenReturn("persona@correo.com");
        when(repositorioUsuario.findByEmailIgnoreCase("persona@correo.com"))
                .thenReturn(Optional.of(usuario(false)));

        MockHttpServletRequest peticion = new MockHttpServletRequest();
        peticion.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token");

        filtro.doFilter(peticion, new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void autenticaUnaCuentaActivaConSuRolActual() throws Exception {
        when(servicioJwt.validarToken("token")).thenReturn(true);
        when(servicioJwt.extraerEmail("token")).thenReturn("persona@correo.com");
        when(repositorioUsuario.findByEmailIgnoreCase("persona@correo.com"))
                .thenReturn(Optional.of(usuario(true)));

        MockHttpServletRequest peticion = new MockHttpServletRequest();
        peticion.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token");

        filtro.doFilter(peticion, new MockHttpServletResponse(), new MockFilterChain());

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("ROLE_PERSONAL",
                SecurityContextHolder.getContext().getAuthentication()
                        .getAuthorities().iterator().next().getAuthority());
    }
}
