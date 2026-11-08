package com.ucab.peluqueria.transferencia;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import com.ucab.peluqueria.modelo.Cita;

public record CitaRespuesta(
        Long id,
        Long servicioId,
        String servicioNombre,
        Long estilistaId,
        String estilistaNombre,
        Long clienteId,
        String clienteNombre,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        BigDecimal costo,
        String estado
) {
    public static CitaRespuesta desde(Cita cita) {
        return new CitaRespuesta(
                cita.getId(),
                cita.getServicio().getId(),
                cita.getServicio().getNombre(),
                cita.getAgenda().getEstilista().getId(),
                cita.getAgenda().getEstilista().getNombre(),
                cita.getCliente().getId(),
                cita.getCliente().getNombre(),
                cita.getAgenda().getFecha(),
                cita.getHoraInicio(),
                cita.getHoraFin(),
                cita.getCosto(),
                cita.getEstado().name()
        );
    }
}
