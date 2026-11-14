package peluqueria.transferencia;

import jakarta.validation.constraints.Size;

/** Motivo opcional de una salida anticipada. El servicio exige al menos 5 caracteres. */
public record SolicitudSalida(
        @Size(max = 200, message = "El motivo no puede superar los 200 caracteres.")
        String motivo) {
}
