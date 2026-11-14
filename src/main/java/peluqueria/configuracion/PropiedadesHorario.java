package peluqueria.configuracion;

import java.time.LocalTime;
import java.time.ZoneId;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Horario del local leído de app.horario.*. Si la apertura no es anterior al cierre,
 * la construcción falla y la aplicación no arranca.
 */
@ConfigurationProperties(prefix = "app.horario")
public record PropiedadesHorario(
        ZoneId zona,
        LocalTime apertura,
        LocalTime cierre,
        int toleranciaMinutos) {

    public PropiedadesHorario {
        if (zona == null || apertura == null || cierre == null) {
            throw new IllegalStateException("El horario del local está incompleto.");
        }
        if (!apertura.isBefore(cierre)) {
            throw new IllegalStateException(
                    "La hora de apertura debe ser anterior a la hora de cierre.");
        }
        if (toleranciaMinutos < 0) {
            throw new IllegalStateException("La tolerancia no puede ser negativa.");
        }
    }
}
