package peluqueria.servicio;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import peluqueria.excepcion.ExcepcionReglaNegocio;
import peluqueria.modelo.Cliente;
import peluqueria.modelo.Rol;
import peluqueria.repositorio.RepositorioCliente;
import peluqueria.repositorio.RepositorioUsuario;
import peluqueria.transferencia.RegistroRequest;
import peluqueria.transferencia.RegistroRespuesta;

/**
 * Registro público de clientes. Encripta la contraseña, asigna el rol CLIENTE y crea la ficha
 * de cliente asociada a la cuenta.
 */
@Service
public class AuthService {

    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioCliente repositorioCliente;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            RepositorioUsuario repositorioUsuario,
            RepositorioCliente repositorioCliente,
            PasswordEncoder passwordEncoder) {
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioCliente = repositorioCliente;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegistroRespuesta registrar(RegistroRequest solicitud) {
        String email = solicitud.email().trim().toLowerCase(Locale.ROOT);
        String cedula = solicitud.cedula().trim();

        if (repositorioUsuario.existsByEmailIgnoreCase(email)) {
            throw ExcepcionReglaNegocio.conflicto(
                    "EMAIL_TAKEN", "Ya existe una cuenta con ese correo.");
        }
        if (repositorioCliente.existsByCedulaIgnoreCase(cedula)) {
            throw ExcepcionReglaNegocio.conflicto(
                    "CEDULA_TAKEN", "Ya existe una cuenta con esa cédula.");
        }

        Cliente cliente = new Cliente();
        cliente.setNombre(solicitud.nombre().trim());
        cliente.setEmail(email);
        cliente.setPassword(passwordEncoder.encode(solicitud.password()));
        cliente.setRol(Rol.CLIENTE);
        cliente.setActivo(true);
        cliente.setCedula(cedula);
        cliente.setTelefono(solicitud.telefono().trim());

        return RegistroRespuesta.desde(repositorioCliente.save(cliente));
    }
}
