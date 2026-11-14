package peluqueria.transferencia;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos editables del perfil del cliente. */
public record SolicitudPerfil(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 120, message = "El nombre no puede tener más de 120 caracteres.")
        String nombre,

        @NotBlank(message = "La cédula es obligatoria.")
        @Size(max = 20, message = "La cédula no puede tener más de 20 caracteres.")
        @Pattern(
                regexp = "^([VEve]-?)?\\d{5,9}$",
                message = "La cédula debe tener entre 5 y 9 dígitos (Ej. V-12345678).")
        String cedula,

        @NotBlank(message = "El teléfono es obligatorio.")
        @Size(max = 30, message = "El teléfono no puede tener más de 30 caracteres.")
        @Pattern(
                regexp = "^\\+?\\d[\\d\\s()-]{6,19}$",
                message = "El teléfono solo puede contener números, espacios, guiones y paréntesis (Ej. 0414-1234567).")
        String telefono) {
}
