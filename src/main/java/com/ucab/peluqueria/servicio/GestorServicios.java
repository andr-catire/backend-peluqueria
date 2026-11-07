package com.ucab.peluqueria.servicio;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ucab.peluqueria.excepcion.ExcepcionReglaNegocio;
import com.ucab.peluqueria.modelo.Servicio;
import com.ucab.peluqueria.repositorio.RepositorioServicio;
import com.ucab.peluqueria.transferencia.ServicioRespuesta;
import com.ucab.peluqueria.transferencia.SolicitudServicio;

/**
 * Reglas de negocio de los servicios. Lo llama ControladorServicio; usa RepositorioServicio.
 */
@Service
public class GestorServicios {

    private final RepositorioServicio repositorioServicio;

    public GestorServicios(RepositorioServicio repositorioServicio) {
        this.repositorioServicio = repositorioServicio;
    }

    @Transactional
    public ServicioRespuesta crear(SolicitudServicio solicitud) {
        String nombre = normalizarNombre(solicitud.nombre());
        Optional<Servicio> existente = repositorioServicio.findByNombreIgnoreCase(nombre);

        if (existente.isPresent()) {
            Servicio servicio = existente.get();

            if (servicio.isActivo()) {
                throw ExcepcionReglaNegocio.conflicto("DUPLICATE", "El servicio ya existe.");
            }

            servicio.setNombre(nombre);
            servicio.setDescripcion(solicitud.descripcion());
            servicio.setCosto(solicitud.costo());
            servicio.setDuracionMinutos(solicitud.duracionMinutos().intValueExact());
            servicio.setActivo(true);

            return ServicioRespuesta.desde(repositorioServicio.save(servicio));
        }

        Servicio servicio = new Servicio();
        servicio.setNombre(nombre);
        servicio.setDescripcion(solicitud.descripcion());
        servicio.setCosto(solicitud.costo());
        servicio.setDuracionMinutos(solicitud.duracionMinutos().intValueExact());
        servicio.setActivo(true);

        return ServicioRespuesta.desde(repositorioServicio.save(servicio));
    }

    private static String normalizarNombre(String nombre) {
        return nombre.trim().replaceAll("\\s+", " ");
    }
}
