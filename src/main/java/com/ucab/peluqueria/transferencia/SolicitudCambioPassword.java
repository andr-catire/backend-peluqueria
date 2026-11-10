package com.ucab.peluqueria.transferencia;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cambio de contraseña del cliente autenticado. */
public record SolicitudCambioPassword(
        @NotBlank(message = "Indica tu contraseña actual.")
        String passwordActual,

        @NotBlank(message = "La nueva contraseña es obligatoria.")
        @Size(min = 8, max = 72, message = "La nueva contraseña debe tener entre 8 y 72 caracteres.")
        String passwordNueva) {
}
