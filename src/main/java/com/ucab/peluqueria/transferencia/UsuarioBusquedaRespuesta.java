package com.ucab.peluqueria.transferencia;

import com.ucab.peluqueria.modelo.Rol;
import com.ucab.peluqueria.modelo.Usuario;

/**
 * Datos básicos que el administrador recibe al buscar una cuenta por correo.
 * Sirven para autocompletar el formulario de promoción a estilista.
 */
public record UsuarioBusquedaRespuesta(Long id, String nombre, String email, Rol rol) {

    public static UsuarioBusquedaRespuesta desde(Usuario usuario) {
        return new UsuarioBusquedaRespuesta(
                usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getRol());
    }
}
