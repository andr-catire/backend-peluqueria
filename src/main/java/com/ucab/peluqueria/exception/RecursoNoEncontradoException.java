package com.ucab.peluqueria.exception;

/** El recurso pedido no existe (servicio, cita, cliente...). Responde 404. */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
