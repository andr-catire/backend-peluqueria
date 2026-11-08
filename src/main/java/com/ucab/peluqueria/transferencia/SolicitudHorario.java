package com.ucab.peluqueria.transferencia;

import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;

/** Nuevo horario de apertura y cierre elegido por el administrador. */
public record SolicitudHorario(
        @NotNull(message = "La hora de apertura es obligatoria.")
        LocalTime apertura,

        @NotNull(message = "La hora de cierre es obligatoria.")
        LocalTime cierre) {
}
