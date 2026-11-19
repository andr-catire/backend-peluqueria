package peluqueria.servicios;

import java.math.BigDecimal;

/**
 * Datos de salida de un servicio. Lo devuelve ControladorServicio a partir de la entidad que
 * entrega GestorServicios.
 */
public record ServicioRespuesta(
        Long id,
        String nombre,
        BigDecimal precio,
        Integer duracionMinutos,
        String descripcion,
        String imagenUrl) {

    public static ServicioRespuesta desde(Servicio servicio) {
        String imagenUrl = servicio.isTieneImagen() && servicio.getId() != null
                ? "/api/servicios/" + servicio.getId() + "/imagen"
                : null;
        return new ServicioRespuesta(
                servicio.getId(),
                servicio.getNombre(),
                servicio.getCosto(),
                servicio.getDuracionMinutos(),
                servicio.getDescripcion(),
                imagenUrl);
    }
}
