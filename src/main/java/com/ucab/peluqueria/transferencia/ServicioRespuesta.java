package com.ucab.peluqueria.transferencia;

import java.math.BigDecimal;

import com.ucab.peluqueria.modelo.Servicio;

/**
 * Datos de salida de un servicio. Lo devuelve ControladorServicio a partir de la entidad que
 * entrega GestorServicios.
 */
public record ServicioRespuesta(Long id, String nombre, BigDecimal costo, Integer duracionMinutos) {

    public static ServicioRespuesta desde(Servicio servicio) {
        return new ServicioRespuesta(
                servicio.getId(),
                servicio.getNombre(),
                servicio.getCosto(),
                servicio.getDuracionMinutos());
    }
}
