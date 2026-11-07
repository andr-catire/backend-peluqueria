package com.ucab.peluqueria.transferencia;

import java.util.Set;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Datos para promover una cuenta a estilista y asignarle servicios.
 * idUsuario identifica la cuenta ya registrada; serviciosIds son los servicios que atenderá.
 */
public record EstilistaRequest(
        @NotNull(message = "El usuario es obligatorio.")
        Long idUsuario,

        @NotEmpty(message = "Debes asignar al menos un servicio.")
        Set<@NotNull(message = "Los identificadores de servicio no pueden ser nulos.") Long> serviciosIds) {
}
