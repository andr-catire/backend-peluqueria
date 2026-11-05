package com.ucab.peluqueria.dto;

import java.math.BigDecimal;

import com.ucab.peluqueria.model.Servicio;

/**
 * Datos de salida de un servicio. Lo devuelve ServicioController a partir de la entidad que
 * entrega ServicioService.
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
