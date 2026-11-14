package peluqueria.transferencia;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

public record SolicitudAgendarCita(
        @NotNull(message = "El servicio es obligatorio")
        Long servicioId,

        @NotNull(message = "El estilista es obligatorio")
        Long estilistaId,

        @NotNull(message = "La fecha es obligatoria")
        @FutureOrPresent(message = "No se puede agendar en fechas pasadas")
        LocalDate fecha,

        @NotNull(message = "La hora de inicio es obligatoria")
        LocalTime horaInicio
) {}
