package com.ucab.peluqueria.transferencia;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos de entrada del registro público. La cuenta siempre nace con rol CLIENTE y su ficha
 * de cliente asociada. Lo usa AuthController y lo valida Spring antes de llegar a AuthService.
 */
public record RegistroRequest(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 120, message = "El nombre no puede tener más de 120 caracteres.")
        String nombre,

        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "El correo no tiene un formato válido.")
        @Size(max = 320, message = "El correo no puede tener más de 320 caracteres.")
        String email,

        @NotBlank(message = "La contraseña es obligatoria.")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres.")
        String password,

        @NotBlank(message = "La cédula es obligatoria.")
        @Size(max = 20, message = "La cédula no puede tener más de 20 caracteres.")
        String cedula,

        @NotBlank(message = "El teléfono es obligatorio.")
        @Size(max = 30, message = "El teléfono no puede tener más de 30 caracteres.")
        String telefono) {
}
