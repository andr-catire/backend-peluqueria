package com.ucab.peluqueria.excepcion;

/** Formato único de error de la API: { "error": "SLOT_TAKEN", "message": "..." }. */
public record ErrorRespuesta(String error, String message) {
}
