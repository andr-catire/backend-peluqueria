package peluqueria.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import peluqueria.excepcion.ExcepcionReglaNegocio;
import peluqueria.modelo.Cliente;
import peluqueria.modelo.Rol;
import peluqueria.repositorio.RepositorioCliente;
import peluqueria.repositorio.RepositorioUsuario;
import peluqueria.transferencia.RegistroRequest;
import peluqueria.transferencia.RegistroRespuesta;

/** Pruebas del registro público: contraseña encriptada, rol CLIENTE y ficha de cliente. */
class PruebasAuthService {

    private RepositorioUsuario repositorioUsuario;
    private RepositorioCliente repositorioCliente;
    private PasswordEncoder passwordEncoder;
    private AuthService servicio;

    @BeforeEach
    void preparar() {
        repositorioUsuario = mock(RepositorioUsuario.class);
        repositorioCliente = mock(RepositorioCliente.class);
        passwordEncoder = mock(PasswordEncoder.class);
        servicio = new AuthService(repositorioUsuario, repositorioCliente, passwordEncoder);
    }

    private static RegistroRequest solicitud() {
        return new RegistroRequest(
                "  Ana Pérez  ", "  Ana@Correo.com ", "secreta123", " V-12345678 ", " 0414-1234567 ");
    }

    @Test
    void registraUnClienteConContrasenaEncriptadaYCorreoNormalizado() {
        when(passwordEncoder.encode("secreta123")).thenReturn("hash-bcrypt");
        when(repositorioCliente.save(any(Cliente.class))).thenAnswer(invocacion -> {
            Cliente guardado = invocacion.getArgument(0);
            guardado.setId(15L);
            return guardado;
        });

        RegistroRespuesta respuesta = servicio.registrar(solicitud());

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(repositorioCliente).save(captor.capture());
        Cliente guardado = captor.getValue();

        assertEquals("Ana Pérez", guardado.getNombre());
        assertEquals("ana@correo.com", guardado.getEmail());
        assertEquals("hash-bcrypt", guardado.getPassword());
        assertEquals(Rol.CLIENTE, guardado.getRol());
        assertEquals("V-12345678", guardado.getCedula());
        assertEquals("0414-1234567", guardado.getTelefono());
        assertEquals(15L, respuesta.id());
        assertEquals(Rol.CLIENTE, respuesta.rol());
    }

    @Test
    void rechazaUnCorreoYaRegistrado() {
        when(repositorioUsuario.existsByEmailIgnoreCase("ana@correo.com")).thenReturn(true);

        ExcepcionReglaNegocio excepcion = assertThrows(
                ExcepcionReglaNegocio.class, () -> servicio.registrar(solicitud()));

        assertEquals("EMAIL_TAKEN", excepcion.getCodigo());
        assertEquals(HttpStatus.CONFLICT, excepcion.getStatus());
        verify(repositorioCliente, never()).save(any());
    }

    @Test
    void rechazaUnaCedulaYaRegistrada() {
        when(repositorioCliente.existsByCedulaIgnoreCase("V-12345678")).thenReturn(true);

        ExcepcionReglaNegocio excepcion = assertThrows(
                ExcepcionReglaNegocio.class, () -> servicio.registrar(solicitud()));

        assertEquals("CEDULA_TAKEN", excepcion.getCodigo());
        verify(passwordEncoder, never()).encode(any());
        verify(repositorioCliente, never()).save(any());
    }
}
