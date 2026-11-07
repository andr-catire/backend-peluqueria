package com.ucab.peluqueria.transferencia;

import java.util.List;

import com.ucab.peluqueria.modelo.Rol;

/** Datos del estilista para el panel de administración y para su propio perfil. */
public record EstilistaRespuesta(
        Long id, String nombre, String email, Rol rol, List<ServicioRespuesta> servicios) {
}
