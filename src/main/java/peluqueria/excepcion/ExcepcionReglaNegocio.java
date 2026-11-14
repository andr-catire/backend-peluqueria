package peluqueria.excepcion;

import org.springframework.http.HttpStatus;

/**
 * Una regla de negocio no se cumple (dato inválido, horario ocupado...).
 * Lleva el código que verá el frontend y el estado HTTP con el que se responde.
 */
public class ExcepcionReglaNegocio extends RuntimeException {

    private final String codigo;
    private final HttpStatus status;

    public ExcepcionReglaNegocio(String codigo, String mensaje, HttpStatus status) {
        super(mensaje);
        this.codigo = codigo;
        this.status = status;
    }

    /** 400 Bad Request. */
    public static ExcepcionReglaNegocio invalida(String codigo, String mensaje) {
        return new ExcepcionReglaNegocio(codigo, mensaje, HttpStatus.BAD_REQUEST);
    }

    /** 409 Conflict. */
    public static ExcepcionReglaNegocio conflicto(String codigo, String mensaje) {
        return new ExcepcionReglaNegocio(codigo, mensaje, HttpStatus.CONFLICT);
    }

    /** 404 Not Found. */
    public static ExcepcionReglaNegocio noEncontrado(String codigo, String mensaje) {
        return new ExcepcionReglaNegocio(codigo, mensaje, HttpStatus.NOT_FOUND);
    }

    public String getCodigo() { return codigo; }
    public HttpStatus getStatus() { return status; }
}
