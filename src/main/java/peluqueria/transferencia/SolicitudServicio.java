package peluqueria.transferencia;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Datos de entrada para crear un servicio. Lo usa ControladorServicio y se valida antes de
 * llegar a GestorServicios.
 */
public record SolicitudServicio(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 120, message = "El nombre no puede tener más de 120 caracteres.")
        String nombre,

        @NotNull(message = "El precio es obligatorio.")
        @Positive(message = "El precio debe ser mayor que 0.")
        @Digits(integer = 8, fraction = 2, message = "El precio debe tener como máximo 8 enteros y 2 decimales.")
        BigDecimal precio,

        @NotNull(message = "La duración es obligatoria.")
        @Positive(message = "La duración debe ser un número entero de minutos mayor que 0")
        @Max(value = 480, message = "La duración no puede superar los 480 minutos.")
        @Digits(integer = 9, fraction = 0,
                message = "La duración debe ser un número entero de minutos mayor que 0")
        BigDecimal duracionMinutos,

        @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres.")
        String descripcion) {
}
