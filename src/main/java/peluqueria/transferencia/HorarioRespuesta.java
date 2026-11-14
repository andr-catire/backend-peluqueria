package peluqueria.transferencia;

import java.time.LocalTime;

import peluqueria.configuracion.PropiedadesHorario;

/** Horario vigente del local para el panel del administrador. */
public record HorarioRespuesta(
        LocalTime apertura,
        LocalTime cierre,
        String zona,
        int toleranciaMinutos) {

    public static HorarioRespuesta desde(PropiedadesHorario horario) {
        return new HorarioRespuesta(
                horario.apertura(),
                horario.cierre(),
                horario.zona().getId(),
                horario.toleranciaMinutos());
    }
}
