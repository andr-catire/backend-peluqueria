package peluqueria.transferencia;

import peluqueria.modelo.Cliente;

/** Datos del perfil que el cliente puede consultar y modificar. */
public record PerfilRespuesta(String nombre, String email, String cedula, String telefono) {

    public static PerfilRespuesta desde(Cliente cliente) {
        return new PerfilRespuesta(
                cliente.getNombre(),
                cliente.getEmail(),
                cliente.getCedula(),
                cliente.getTelefono());
    }
}
