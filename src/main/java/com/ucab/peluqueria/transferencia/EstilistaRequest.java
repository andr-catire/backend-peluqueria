package com.ucab.peluqueria.transferencia;

import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear una cuenta de estilista. La usa ControladorEstilista y la valida Spring
 * antes de llegar a GestorEstilistas.
 */
public record EstilistaRequest(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 120, message = "El nombre no puede tener más de 120 caracteres.")
        String nombre,

        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "El correo no tiene un formato válido.")
        @Size(max = 320, message = "El correo no puede tener más de 320 caracteres.")
        String email,

        @NotBlank(message = "La contraseña inicial es obligatoria.")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres.")
        String password,

        @NotEmpty(message = "Debes asignar al menos un servicio.")
        @Size(min = 1, max = 50, message = "Puedes asignar entre 1 y 50 servicios.")
        Set<@NotNull(message = "Los identificadores de servicio no pueden ser nulos.") Long> serviciosIds) {
}
