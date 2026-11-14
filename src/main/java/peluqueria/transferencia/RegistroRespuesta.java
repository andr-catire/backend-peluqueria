package peluqueria.transferencia;

import peluqueria.modelo.Cliente;
import peluqueria.modelo.Rol;

/**
 * Respuesta del registro público. No incluye token: el inicio de sesión pertenece a otra
 * historia; aquí solo se confirma la cuenta creada.
 */
public record RegistroRespuesta(Long id, String nombre, String email, Rol rol) {

    public static RegistroRespuesta desde(Cliente cliente) {
        return new RegistroRespuesta(
                cliente.getId(), cliente.getNombre(), cliente.getEmail(), cliente.getRol());
    }
}
