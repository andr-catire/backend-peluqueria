package com.ucab.peluqueria.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ucab.peluqueria.excepcion.ExcepcionRecursoNoEncontrado;
import com.ucab.peluqueria.excepcion.ExcepcionReglaNegocio;
import com.ucab.peluqueria.modelo.Cliente;
import com.ucab.peluqueria.modelo.Rol;
import com.ucab.peluqueria.repositorio.RepositorioCliente;
import com.ucab.peluqueria.seguridad.UsuarioActual;
import com.ucab.peluqueria.transferencia.PerfilRespuesta;
import com.ucab.peluqueria.transferencia.SolicitudCambioPassword;
import com.ucab.peluqueria.transferencia.SolicitudPerfil;

/** Pruebas de las reglas de negocio del perfil del cliente. */
class PruebasGestorPerfil {

    private UsuarioActual usuarioActual;
    private RepositorioCliente repositorioCliente;
    private PasswordEncoder passwordEncoder;
    private GestorPerfil gestor;

    @BeforeEach
    void preparar() {
        usuarioActual = mock(UsuarioActual.class);
        repositorioCliente = mock(RepositorioCliente.class);
        passwordEncoder = mock(PasswordEncoder.class);
        gestor = new GestorPerfil(usuarioActual, repositorioCliente, passwordEncoder);
    }

    private static Cliente cliente(Long id, String cedula) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNombre("Ana Pérez");
        cliente.setEmail("ana@correo.com");
        cliente.setPassword("hash-actual");
        cliente.setRol(Rol.CLIENTE);
        cliente.setActivo(true);
        cliente.setCedula(cedula);
        cliente.setTelefono("0414-1234567");
        return cliente;
    }

    @Test
    void obtieneElPerfilDelClienteAutenticado() {
        Cliente cliente = cliente(7L, "V-12345678");
        when(usuarioActual.id()).thenReturn(7L);
        when(repositorioCliente.findById(7L)).thenReturn(Optional.of(cliente));

        PerfilRespuesta respuesta = gestor.obtener();

        assertEquals("Ana Pérez", respuesta.nombre());
        assertEquals("ana@correo.com", respuesta.email());
        assertEquals("V-12345678", respuesta.cedula());
        assertEquals("0414-1234567", respuesta.telefono());
    }

    @Test
    void actualizaLosDatosYRecortaEspacios() {
        Cliente cliente = cliente(7L, "V-12345678");
        when(usuarioActual.id()).thenReturn(7L);
        when(repositorioCliente.findById(7L)).thenReturn(Optional.of(cliente));
        when(repositorioCliente.findByCedulaIgnoreCase("V-87654321")).thenReturn(Optional.empty());
        when(repositorioCliente.save(any(Cliente.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        PerfilRespuesta respuesta = gestor.actualizar(
                new SolicitudPerfil(" Ana Pérez ", " V-87654321 ", " 0414-7654321 "));

        assertEquals("Ana Pérez", cliente.getNombre());
        assertEquals("V-87654321", cliente.getCedula());
        assertEquals("0414-7654321", cliente.getTelefono());
        assertEquals("V-87654321", respuesta.cedula());
        verify(repositorioCliente).save(cliente);
    }

    @Test
    void permiteConservarLaCedulaDelMismoCliente() {
        Cliente cliente = cliente(7L, "V-12345678");
        when(usuarioActual.id()).thenReturn(7L);
        when(repositorioCliente.findById(7L)).thenReturn(Optional.of(cliente));
        when(repositorioCliente.findByCedulaIgnoreCase("V-12345678"))
                .thenReturn(Optional.of(cliente));
        when(repositorioCliente.save(any(Cliente.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        gestor.actualizar(new SolicitudPerfil("Ana Pérez", "V-12345678", "0414-1234567"));

        verify(repositorioCliente).save(cliente);
    }

    @Test
    void rechazaLaCedulaDeOtroCliente() {
        Cliente cliente = cliente(7L, "V-12345678");
        Cliente otro = cliente(8L, "V-87654321");
        when(usuarioActual.id()).thenReturn(7L);
        when(repositorioCliente.findById(7L)).thenReturn(Optional.of(cliente));
        when(repositorioCliente.findByCedulaIgnoreCase("V-87654321")).thenReturn(Optional.of(otro));

        ExcepcionReglaNegocio excepcion = assertThrows(ExcepcionReglaNegocio.class,
                () -> gestor.actualizar(
                        new SolicitudPerfil("Ana Pérez", "V-87654321", "0414-1234567")));

        assertEquals("CEDULA_TAKEN", excepcion.getCodigo());
        assertEquals(HttpStatus.CONFLICT, excepcion.getStatus());
        verify(repositorioCliente, never()).save(any());
    }

    @Test
    void rechazaUnaContrasenaActualIncorrecta() {
        Cliente cliente = cliente(7L, "V-12345678");
        when(usuarioActual.id()).thenReturn(7L);
        when(repositorioCliente.findById(7L)).thenReturn(Optional.of(cliente));
        when(passwordEncoder.matches("incorrecta", "hash-actual")).thenReturn(false);

        ExcepcionReglaNegocio excepcion = assertThrows(ExcepcionReglaNegocio.class,
                () -> gestor.cambiarPassword(
                        new SolicitudCambioPassword("incorrecta", "nuevaClave123")));

        assertEquals("PASSWORD_INCORRECT", excepcion.getCodigo());
        assertEquals(HttpStatus.BAD_REQUEST, excepcion.getStatus());
        verify(passwordEncoder, never()).encode(any());
        verify(repositorioCliente, never()).save(any());
    }

    @Test
    void cambiaLaContrasenaCuandoLaActualEsCorrecta() {
        Cliente cliente = cliente(7L, "V-12345678");
        when(usuarioActual.id()).thenReturn(7L);
        when(repositorioCliente.findById(7L)).thenReturn(Optional.of(cliente));
        when(passwordEncoder.matches("actual123", "hash-actual")).thenReturn(true);
        when(passwordEncoder.encode("nuevaClave123")).thenReturn("hash-nuevo");

        gestor.cambiarPassword(new SolicitudCambioPassword("actual123", "nuevaClave123"));

        assertEquals("hash-nuevo", cliente.getPassword());
        verify(repositorioCliente).save(cliente);
    }

    @Test
    void respondeNoEncontradoSiElClienteYaNoExiste() {
        when(usuarioActual.id()).thenReturn(7L);
        when(repositorioCliente.findById(7L)).thenReturn(Optional.empty());

        assertThrows(ExcepcionRecursoNoEncontrado.class, () -> gestor.obtener());
    }
}
