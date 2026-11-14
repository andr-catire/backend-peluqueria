package peluqueria.seguridad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import peluqueria.modelo.Rol;
import peluqueria.modelo.Usuario;
import peluqueria.repositorio.RepositorioUsuario;

class PruebasServicioDetallesUsuario {

    private RepositorioUsuario repositorio;
    private ServicioDetallesUsuario servicio;

    @BeforeEach
    void preparar() {
        repositorio = mock(RepositorioUsuario.class);
        servicio = new ServicioDetallesUsuario(repositorio);
    }

    private static Usuario usuario(String email, Rol rol, boolean activo) {
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setPassword("hash-bcrypt");
        usuario.setRol(rol);
        usuario.setActivo(activo);
        return usuario;
    }

    @Test
    void cargaLaCuentaNormalizandoElCorreoYAsignaElRol() {
        when(repositorio.findByEmailIgnoreCase("ana@correo.com"))
                .thenReturn(Optional.of(usuario("ana@correo.com", Rol.CLIENTE, true)));

        UserDetails detalles = servicio.loadUserByUsername("  Ana@Correo.com ");

        assertEquals("ana@correo.com", detalles.getUsername());
        assertEquals("hash-bcrypt", detalles.getPassword());
        assertTrue(detalles.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE")));
        assertTrue(detalles.isEnabled());
    }

    @Test
    void unaCuentaInactivaQuedaDeshabilitada() {
        when(repositorio.findByEmailIgnoreCase("luis@correo.com"))
                .thenReturn(Optional.of(usuario("luis@correo.com", Rol.PERSONAL, false)));

        assertFalse(servicio.loadUserByUsername("luis@correo.com").isEnabled());
    }

    @Test
    void siNoExisteLanzaUsernameNotFound() {
        when(repositorio.findByEmailIgnoreCase("nadie@correo.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> servicio.loadUserByUsername("nadie@correo.com"));
    }
}
