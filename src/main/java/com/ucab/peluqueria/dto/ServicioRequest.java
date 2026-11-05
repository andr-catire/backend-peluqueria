package com.ucab.peluqueria.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Datos de entrada para crear un servicio. Lo usa ServicioController y se valida antes de
 * llegar a ServicioService.
 */
public record ServicioRequest(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 120, message = "El nombre no puede tener más de 120 caracteres.")
        String nombre,

        @NotNull(message = "El costo es obligatorio.")
        @DecimalMin(value = "0.00", inclusive = false, message = "El costo debe ser mayor que 0.")
        @Digits(integer = 8, fraction = 2, message = "El costo debe tener como máximo 8 enteros y 2 decimales.")
        BigDecimal costo,

        @NotNull(message = "La duración es obligatoria.")
        @Positive(message = "La duración debe ser un número entero de minutos mayor que 0")
        @Digits(integer = 9, fraction = 0,
                message = "La duración debe ser un número entero de minutos mayor que 0")
        BigDecimal duracionMinutos) {
}
