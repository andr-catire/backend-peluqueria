package peluqueria.excepcion;

/** El recurso pedido no existe (servicio, cita, cliente...). Responde 404. */
public class ExcepcionRecursoNoEncontrado extends RuntimeException {

    public ExcepcionRecursoNoEncontrado(String mensaje) {
        super(mensaje);
    }
}
