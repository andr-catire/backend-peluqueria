package com.ucab.peluqueria.servicio;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ucab.peluqueria.excepcion.ExcepcionReglaNegocio;
import com.ucab.peluqueria.modelo.Cliente;
import com.ucab.peluqueria.modelo.Rol;
import com.ucab.peluqueria.repositorio.RepositorioCliente;
import com.ucab.peluqueria.repositorio.RepositorioUsuario;
import com.ucab.peluqueria.transferencia.RegistroRequest;
import com.ucab.peluqueria.transferencia.RegistroRespuesta;

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
