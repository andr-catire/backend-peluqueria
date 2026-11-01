package com.ucab.peluqueria.exception;

import java.sql.SQLException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.ucab.peluqueria.dto.ErrorRespuesta;

/**
 * Manejo global de errores de la API. Extiende ResponseEntityExceptionHandler para que los errores
 * del propio Spring MVC (JSON mal formado, método no permitido, ruta inexistente...) también salgan
 * con el formato { "error", "message" } y el estado HTTP correcto, en lugar de un 500.
 */
@RestControllerAdvice
public class ManejadorGlobalExcepciones extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ManejadorGlobalExcepciones.class);

    /** exclusion_violation: la restricción de PostgreSQL contra citas solapadas del mismo estilista. */
    static final String SQLSTATE_EXCLUSION = "23P01";
    /** unique_violation. */
    static final String SQLSTATE_UNICO = "23505";

    // ---------- Errores de negocio ----------

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorRespuesta> manejarNoEncontrado(RecursoNoEncontradoException ex) {
        return responder(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorRespuesta> manejarReglaNegocio(ReglaNegocioException ex) {
        return responder(ex.getStatus(), ex.getCodigo(), ex.getMessage());
    }

    // ---------- Base de datos ----------

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorRespuesta> manejarIntegridad(DataIntegrityViolationException ex) {
        String sqlState = buscarSqlState(ex);
        if (SQLSTATE_EXCLUSION.equals(sqlState)) {
            return responder(HttpStatus.CONFLICT, "SLOT_TAKEN",
                    "Ese horario ya no está disponible para el estilista elegido.");
        }
        if (SQLSTATE_UNICO.equals(sqlState)) {
            return responder(HttpStatus.CONFLICT, "DUPLICATE", "Ya existe un registro con esos datos.");
        }
        log.warn("Violación de integridad de datos: {}", ex.getMostSpecificCause().getMessage());
        return responder(HttpStatus.CONFLICT, "DATA_CONFLICT", "Los datos entran en conflicto con los existentes.");
    }

    // ---------- Seguridad ----------

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorRespuesta> manejarAccesoDenegado(AccessDeniedException ex) {
        return responder(HttpStatus.FORBIDDEN, "FORBIDDEN", "No tienes permisos para realizar esta acción.");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorRespuesta> manejarAutenticacion(AuthenticationException ex) {
        return responder(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Inicia sesión para continuar.");
    }

    // ---------- Cualquier otro error: nunca se filtra el detalle al cliente ----------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuesta> manejarInesperado(Exception ex) {
        log.error("Error inesperado", ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Ocurrió un error al procesar la solicitud.");
    }

    // ---------- Errores del propio Spring MVC ----------

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("; "));
        String mensaje = detalle.isBlank() ? "Hay datos inválidos en la solicitud." : detalle;
        return new ResponseEntity<>(new ErrorRespuesta("VALIDATION_ERROR", mensaje), HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
            HttpStatusCode statusCode, WebRequest request) {
        ErrorRespuesta cuerpo = new ErrorRespuesta(codigoPara(statusCode), mensajePara(statusCode));
        return new ResponseEntity<>(cuerpo, headers, statusCode);
    }

    // ---------- Utilidades ----------

    private static ResponseEntity<ErrorRespuesta> responder(HttpStatus status, String codigo, String mensaje) {
        return ResponseEntity.status(status).body(new ErrorRespuesta(codigo, mensaje));
    }

    /** Recorre la cadena de causas buscando el SQLSTATE de PostgreSQL. */
    static String buscarSqlState(Throwable error) {
        Throwable actual = error;
        while (actual != null) {
            if (actual instanceof SQLException sql && sql.getSQLState() != null) {
                return sql.getSQLState();
            }
            actual = actual.getCause() == actual ? null : actual.getCause();
        }
        return null;
    }

    private static String codigoPara(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> "BAD_REQUEST";
            case 401 -> "UNAUTHORIZED";
            case 403 -> "FORBIDDEN";
            case 404 -> "NOT_FOUND";
            case 405 -> "METHOD_NOT_ALLOWED";
            case 406 -> "NOT_ACCEPTABLE";
            case 415 -> "UNSUPPORTED_MEDIA_TYPE";
            default -> status.is5xxServerError() ? "INTERNAL_ERROR" : "ERROR";
        };
    }

    private static String mensajePara(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> "La solicitud no es válida.";
            case 401 -> "Inicia sesión para continuar.";
            case 403 -> "No tienes permisos para realizar esta acción.";
            case 404 -> "No se encontró lo que buscas.";
            case 405 -> "Ese método no está permitido en esta ruta.";
            case 406 -> "Formato de respuesta no disponible.";
            case 415 -> "Formato de datos no soportado.";
            default -> status.is5xxServerError()
                    ? "Ocurrió un error al procesar la solicitud."
                    : "No se pudo procesar la solicitud.";
        };
    }
}
