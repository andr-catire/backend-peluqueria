package com.ucab.peluqueria.servicio;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ucab.peluqueria.excepcion.ExcepcionRecursoNoEncontrado;
import com.ucab.peluqueria.excepcion.ExcepcionReglaNegocio;
import com.ucab.peluqueria.modelo.Cliente;
import com.ucab.peluqueria.repositorio.RepositorioCliente;
import com.ucab.peluqueria.seguridad.UsuarioActual;
import com.ucab.peluqueria.transferencia.PerfilRespuesta;
import com.ucab.peluqueria.transferencia.SolicitudCambioPassword;
import com.ucab.peluqueria.transferencia.SolicitudPerfil;

/**
 * Reglas de negocio del perfil del cliente autenticado. El id siempre sale del token,
 * nunca de la URL, para que un cliente no pueda modificar la cuenta de otro.
 */
@Service
public class GestorPerfil {

    private final UsuarioActual usuarioActual;
    private final RepositorioCliente repositorioCliente;
    private final PasswordEncoder passwordEncoder;

    public GestorPerfil(
            UsuarioActual usuarioActual,
            RepositorioCliente repositorioCliente,
            PasswordEncoder passwordEncoder) {
        this.usuarioActual = usuarioActual;
        this.repositorioCliente = repositorioCliente;
        this.passwordEncoder = passwordEncoder;
    }

    /** Devuelve los datos actuales del cliente autenticado. */
    @Transactional(readOnly = true)
    public PerfilRespuesta obtener() {
        return PerfilRespuesta.desde(clienteActual());
    }

    /** Actualiza nombre, cédula y teléfono. La cédula sigue siendo única entre clientes. */
    @Transactional
    public PerfilRespuesta actualizar(SolicitudPerfil solicitud) {
        Cliente cliente = clienteActual();
        String cedula = solicitud.cedula().trim();

        repositorioCliente.findByCedulaIgnoreCase(cedula)
                .filter(otro -> !otro.getId().equals(cliente.getId()))
                .ifPresent(otro -> {
                    throw ExcepcionReglaNegocio.conflicto(
                            "CEDULA_TAKEN", "Ya existe una cuenta con esa cédula.");
                });

        cliente.setNombre(solicitud.nombre().trim());
        cliente.setCedula(cedula);
        cliente.setTelefono(solicitud.telefono().trim());

        return PerfilRespuesta.desde(repositorioCliente.save(cliente));
    }

    /** Cambia la contraseña solo si la actual coincide. */
    @Transactional
    public void cambiarPassword(SolicitudCambioPassword solicitud) {
        Cliente cliente = clienteActual();

        if (!passwordEncoder.matches(solicitud.passwordActual(), cliente.getPassword())) {
            throw ExcepcionReglaNegocio.invalida(
                    "PASSWORD_INCORRECT", "La contraseña actual no es correcta.");
        }

        cliente.setPassword(passwordEncoder.encode(solicitud.passwordNueva()));
        repositorioCliente.save(cliente);
    }

    private Cliente clienteActual() {
        return repositorioCliente.findById(usuarioActual.id())
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado(
                        "No se encontró el perfil del cliente autenticado."));
    }
}
