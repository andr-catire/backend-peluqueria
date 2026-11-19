package com.ucab.peluqueria.perfil;

import com.ucab.peluqueria.clientes.Cliente;

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
