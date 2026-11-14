package peluqueria.transferencia;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

/**
 * Estado del día de asistencia del empleado autenticado. La vista solo refleja estas reglas,
 * calculadas en el servidor.
 */
public record AsistenciaHoyRespuesta(
        LocalDate fecha,
        LocalTime horaActual,
        LocalTime horaApertura,
        LocalTime horaCierre,
        LocalTime horaEntrada,
        LocalTime horaSalida,
        boolean llegoTarde,
        int minutosTarde,
        boolean salidaAnticipada,
        boolean puedeRegistrarEntrada,
        boolean puedeRegistrarSalida,
        OffsetDateTime salidaHabilitadaDesde) {
}
