package com.ucab.peluqueria.configuracion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ucab.peluqueria.modelo.Rol;
import com.ucab.peluqueria.modelo.Usuario;
import com.ucab.peluqueria.repositorio.RepositorioUsuario;

class PruebasAdminInicializador {

    private RepositorioUsuario repositorio;
    // Se usa BCrypt con pocas rondas: la prueba es más rápida y verifica un hash real.
    private final PasswordEncoder codificador = new BCryptPasswordEncoder(4);

    @BeforeEach
    void preparar() {
        repositorio = mock(RepositorioUsuario.class);
    }

    private AdminInicializador crear(String email, String password) {
        return new AdminInicializador(repositorio, codificador, email, password);
    }

    @Test
    void creaElAdministradorSiNoExisteNinguno() {
        when(repositorio.existsByRol(Rol.ADMIN)).thenReturn(false);

        crear("  Admin@Peluqueria.com ", "clave-segura-123").run();

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repositorio).save(captor.capture());
        Usuario guardado = captor.getValue();
        assertEquals("admin@peluqueria.com", guardado.getEmail());
        assertEquals(Rol.ADMIN, guardado.getRol());
        assertNotEquals("clave-segura-123", guardado.getPassword());
        assertEquals(true, codificador.matches("clave-segura-123", guardado.getPassword()));
    }

    @Test
    void noHaceNadaSiYaExisteUnAdministrador() {
        when(repositorio.existsByRol(Rol.ADMIN)).thenReturn(true);

        crear("admin@peluqueria.com", "clave-segura-123").run();

        verify(repositorio, never()).save(any());
    }

    @Test
    void noCreaNadaSiFaltanLasCredenciales() {
        when(repositorio.existsByRol(Rol.ADMIN)).thenReturn(false);

        crear("", "").run();
        crear("admin@peluqueria.com", "  ").run();

        verify(repositorio, never()).save(any());
    }

    @Test
    void rechazaUnaContrasenaCorta() {
        when(repositorio.existsByRol(Rol.ADMIN)).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> crear("admin@peluqueria.com", "1234").run());
        verify(repositorio, never()).save(any());
    }

    @Test
    void rechazaUnCorreoInvalido() {
        when(repositorio.existsByRol(Rol.ADMIN)).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> crear("no-es-un-correo", "clave-segura-123").run());
        verify(repositorio, never()).save(any());
    }
}
