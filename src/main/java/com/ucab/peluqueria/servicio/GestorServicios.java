package com.ucab.peluqueria.servicio;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ucab.peluqueria.excepcion.ExcepcionReglaNegocio;
import com.ucab.peluqueria.modelo.Servicio;
import com.ucab.peluqueria.repositorio.RepositorioServicio;
import com.ucab.peluqueria.transferencia.ServicioRespuesta;
import com.ucab.peluqueria.transferencia.SolicitudServicio;

/**
 * Reglas de negocio de los servicios. Lo llama ControladorServicio; usa RepositorioServicio y GestorImagenes.
 */
@Service
public class GestorServicios {

    private final RepositorioServicio repositorioServicio;
    private final GestorImagenes gestorImagenes;

    public GestorServicios(RepositorioServicio repositorioServicio, GestorImagenes gestorImagenes) {
        this.repositorioServicio = repositorioServicio;
        this.gestorImagenes = gestorImagenes;
    }

    /**
     * Crea un servicio activo. Si el nombre pertenece a un servicio inactivo, lo reactiva y
     * responde 201 con ese mismo id; si el servicio ya está activo, lanza DUPLICATE.
     */
    @Transactional
    public ServicioRespuesta crear(SolicitudServicio solicitud) {
        return ServicioRespuesta.desde(guardarServicio(solicitud));
    }

    /** Crea o reactiva el servicio y, si llega una imagen, la guarda en el mismo id. */
    @Transactional
    public ServicioRespuesta crear(SolicitudServicio solicitud, MultipartFile imagen) throws IOException {
        Servicio servicio = guardarServicio(solicitud);

        if (imagen != null && !imagen.isEmpty()) {
            gestorImagenes.guardar(servicio.getId(), imagen);
            servicio.setTieneImagen(true);
            servicio = repositorioServicio.save(servicio);
        }

        return ServicioRespuesta.desde(servicio);
    }

    /** Lista los servicios activos ordenados por nombre. Lo usa el GET público. */
    @Transactional(readOnly = true)
    public List<ServicioRespuesta> listar() {
        return repositorioServicio.findByActivoTrueOrderByNombreAsc().stream()
                .map(ServicioRespuesta::desde)
                .toList();
    }

    private Servicio guardarServicio(SolicitudServicio solicitud) {
        String nombre = normalizarNombre(solicitud.nombre());
        Optional<Servicio> existente = repositorioServicio.findByNombreIgnoreCase(nombre);

        if (existente.isPresent()) {
            Servicio servicio = existente.get();

            if (servicio.isActivo()) {
                throw ExcepcionReglaNegocio.conflicto(
                        "DUPLICATE", "Ya existe un servicio con ese nombre.");
            }

            servicio.setNombre(nombre);
            servicio.setDescripcion(normalizarDescripcion(solicitud.descripcion()));
            servicio.setCosto(solicitud.costo());
            servicio.setDuracionMinutos(solicitud.duracionMinutos().intValueExact());
            servicio.setActivo(true);

            return repositorioServicio.save(servicio);
        }

        Servicio servicio = new Servicio();
        servicio.setNombre(nombre);
        servicio.setDescripcion(normalizarDescripcion(solicitud.descripcion()));
        servicio.setCosto(solicitud.costo());
        servicio.setDuracionMinutos(solicitud.duracionMinutos().intValueExact());
        servicio.setActivo(true);

        return repositorioServicio.save(servicio);
    }

    private static String normalizarNombre(String nombre) {
        return nombre.trim().replaceAll("\\s+", " ");
    }

    private static String normalizarDescripcion(String descripcion) {
        return descripcion == null || descripcion.isBlank() ? null : descripcion.trim();
    }
}
