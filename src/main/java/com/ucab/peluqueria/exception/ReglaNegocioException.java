package com.ucab.peluqueria.exception;

import org.springframework.http.HttpStatus;

/**
 * Una regla de negocio no se cumple (dato inválido, horario ocupado...).
 * Lleva el código que verá el frontend y el estado HTTP con el que se responde.
 */
public class ReglaNegocioException extends RuntimeException {

    private final String codigo;
    private final HttpStatus status;

    public ReglaNegocioException(String codigo, String mensaje, HttpStatus status) {
        super(mensaje);
        this.codigo = codigo;
        this.status = status;
    }

    /** 400 Bad Request. */
    public static ReglaNegocioException invalida(String codigo, String mensaje) {
        return new ReglaNegocioException(codigo, mensaje, HttpStatus.BAD_REQUEST);
    }

    /** 409 Conflict. */
    public static ReglaNegocioException conflicto(String codigo, String mensaje) {
        return new ReglaNegocioException(codigo, mensaje, HttpStatus.CONFLICT);
    }

    public String getCodigo() { return codigo; }
    public HttpStatus getStatus() { return status; }
}
