package com.ucab.peluqueria.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ucab.peluqueria.dto.ServicioRequest;
import com.ucab.peluqueria.dto.ServicioRespuesta;
import com.ucab.peluqueria.exception.ReglaNegocioException;
import com.ucab.peluqueria.model.Servicio;
import com.ucab.peluqueria.repository.ServicioRepository;

/**
 * Reglas de negocio de los servicios. Lo llama ServicioController; usa ServicioRepository.
 */
@Service
public class ServicioService {

    private final ServicioRepository servicioRepository;

    public ServicioService(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    @Transactional
    public ServicioRespuesta crear(ServicioRequest solicitud) {
        String nombre = normalizarNombre(solicitud.nombre());

        if (servicioRepository.existsByNombreIgnoreCaseAndActivoTrue(nombre)) {
            throw ReglaNegocioException.conflicto("DUPLICATE", "Ya existe un servicio con ese nombre.");
        }

        Servicio servicio = new Servicio();
        servicio.setNombre(nombre);
        servicio.setCosto(solicitud.costo());
        servicio.setDuracionMinutos(solicitud.duracionMinutos().intValueExact());
        servicio.setActivo(true);

        return ServicioRespuesta.desde(servicioRepository.save(servicio));
    }

    private static String normalizarNombre(String nombre) {
        return nombre.trim().replaceAll("\\s+", " ");
    }
}
