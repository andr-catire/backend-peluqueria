package com.ucab.peluqueria.transferencia;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Datos requeridos para iniciar sesión en la API.
 */
public record SolicitudLogin(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El formato de correo no es válido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {}
