package peluqueria.transferencia;

import java.time.LocalTime;

/**
 * Fila de asistencia de un estilista para el panel del administrador. Si no hay entrada,
 * trabajo queda en false y las horas en null.
 */
public record AsistenciaPersonalRespuesta(
        Long personalId,
        String nombre,
        String email,
        LocalTime horaEntrada,
        LocalTime horaSalida,
        boolean llegoTarde,
        int minutosTarde,
        boolean salidaAnticipada,
        String motivoSalida,
        boolean trabajo) {
}
