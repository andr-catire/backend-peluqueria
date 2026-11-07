package com.ucab.peluqueria.servicio;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ucab.peluqueria.excepcion.ExcepcionRecursoNoEncontrado;
import com.ucab.peluqueria.excepcion.ExcepcionReglaNegocio;
import com.ucab.peluqueria.modelo.Rol;
import com.ucab.peluqueria.modelo.Servicio;
import com.ucab.peluqueria.modelo.Usuario;
import com.ucab.peluqueria.repositorio.RepositorioCliente;
import com.ucab.peluqueria.repositorio.RepositorioEstilista;
import com.ucab.peluqueria.repositorio.RepositorioServicio;
import com.ucab.peluqueria.repositorio.RepositorioUsuario;
import com.ucab.peluqueria.transferencia.EstilistaRequest;
import com.ucab.peluqueria.transferencia.EstilistaRespuesta;
import com.ucab.peluqueria.transferencia.ServicioRespuesta;
import com.ucab.peluqueria.transferencia.UsuarioBusquedaRespuesta;

/**
 * Reglas de negocio del personal: búsqueda de cuentas, promoción a estilista, consulta del
 * propio perfil y desvinculación segura.
 */
@Service
public class EstilistaService {

    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioCliente repositorioCliente;
    private final RepositorioEstilista repositorioEstilista;
    private final RepositorioServicio repositorioServicio;

    public EstilistaService(
            RepositorioUsuario repositorioUsuario,
            RepositorioCliente repositorioCliente,
            RepositorioEstilista repositorioEstilista,
            RepositorioServicio repositorioServicio) {
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioCliente = repositorioCliente;
        this.repositorioEstilista = repositorioEstilista;
        this.repositorioServicio = repositorioServicio;
    }

    /** Paso 2: datos básicos de una cuenta CLIENTE por correo para autocompletar el formulario. */
    @Transactional(readOnly = true)
    public UsuarioBusquedaRespuesta buscarPorEmail(String email) {
        if (email == null || email.isBlank()) {
            throw ExcepcionReglaNegocio.invalida(
                    "EMAIL_REQUIRED", "Indica el correo de la cuenta que quieres buscar.");
        }

        Usuario usuario = repositorioUsuario
                .findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado(
                        "No existe una cuenta con ese correo."));

        if (usuario.getRol() != Rol.CLIENTE) {
            throw ExcepcionReglaNegocio.conflicto(
                    "NOT_PROMOTABLE",
                    "La cuenta ya tiene un rol que no se puede promover a estilista.");
        }

        return UsuarioBusquedaRespuesta.desde(usuario);
    }

    /**
     * Paso 3: promueve una cuenta CLIENTE a estilista y le asigna los servicios indicados.
     *
     * <p>La cuenta deja de ser cliente: si tiene ficha en clientes se elimina para no dejar dos
     * perfiles sobre el mismo usuario. Si esa ficha tiene citas, no se puede borrar por la FK de
     * citas y la promoción se bloquea hasta que el administrador las reasigne o cancele.</p>
     */
    @Transactional
    public EstilistaRespuesta promoverAEmpleado(EstilistaRequest solicitud) {
        Usuario usuario = repositorioUsuario.findById(solicitud.idUsuario())
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado(
                        "No existe la cuenta que quieres promover."));

        if (usuario.getRol() == Rol.ADMIN) {
            throw ExcepcionReglaNegocio.conflicto(
                    "INVALID_ROLE", "No se puede promover una cuenta administradora.");
        }
        if (usuario.getRol() == Rol.PERSONAL) {
            throw ExcepcionReglaNegocio.conflicto(
                    "ALREADY_EMPLOYEE", "La cuenta ya es estilista.");
        }

        List<Servicio> servicios = cargarServicios(solicitud.serviciosIds());

        boolean tieneFichaCliente = repositorioCliente.existePerfil(usuario.getId());
        if (tieneFichaCliente && repositorioCliente.tieneCitas(usuario.getId())) {
            throw ExcepcionReglaNegocio.conflicto(
                    "CLIENT_WITH_APPOINTMENTS",
                    "La cuenta tiene citas registradas como cliente. Reasígnalas o cancélalas antes de promoverla.");
        }

        usuario.setRol(Rol.PERSONAL);
        repositorioUsuario.saveAndFlush(usuario);

        // Una sola cuenta: la ficha de cliente desaparece y nace la de estilista.
        if (tieneFichaCliente) {
            repositorioCliente.eliminarPerfil(usuario.getId());
        }
        repositorioEstilista.insertarPerfil(usuario.getId());
        for (Servicio servicio : servicios) {
            repositorioEstilista.asignarServicio(usuario.getId(), servicio.getId());
        }

        return respuestaDesde(usuario, servicios);
    }

    /** Paso 4: perfil del estilista autenticado, con sus servicios asignados. */
    @Transactional(readOnly = true)
    public EstilistaRespuesta obtenerMiPerfil(Long idUsuario) {
        if (!repositorioEstilista.existePerfil(idUsuario)) {
            throw new ExcepcionRecursoNoEncontrado("No se encontró el perfil del estilista.");
        }

        Usuario usuario = repositorioUsuario.findById(idUsuario)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado(
                        "No se encontró el perfil del estilista."));
        List<Servicio> servicios = repositorioEstilista.buscarServicios(idUsuario);

        return respuestaDesde(usuario, servicios);
    }

    /**
     * Paso 5: desvincula al estilista sin borrar la fila personal. Las citas y asistencias la
     * referencian, así que solo se cambia el rol a CLIENTE y se vacían sus servicios.
     */
    @Transactional
    public void desvincularEstilista(Long idEstilista) {
        if (!repositorioEstilista.existePerfil(idEstilista)) {
            throw new ExcepcionRecursoNoEncontrado(
                    "No existe el estilista que quieres desvincular.");
        }

        Usuario usuario = repositorioUsuario.findById(idEstilista)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado(
                        "No existe el estilista que quieres desvincular."));

        if (usuario.getRol() != Rol.PERSONAL) {
            throw ExcepcionReglaNegocio.conflicto(
                    "NOT_EMPLOYEE", "La cuenta indicada no es un estilista activo.");
        }

        if (repositorioEstilista.tieneCitasFuturasActivas(idEstilista)) {
            throw ExcepcionReglaNegocio.conflicto(
                    "PENDING_APPOINTMENTS",
                    "El estilista tiene citas pendientes o confirmadas. Reasígnalas o cancélalas antes de desvincularlo.");
        }

        repositorioEstilista.eliminarServicios(idEstilista);
        repositorioUsuario.actualizarRol(idEstilista, Rol.CLIENTE);
    }

    private List<Servicio> cargarServicios(Set<Long> ids) {
        List<Servicio> servicios = repositorioServicio.findAllById(ids);

        if (servicios.size() != ids.size()) {
            Set<Long> encontrados = servicios.stream()
                    .map(Servicio::getId)
                    .collect(Collectors.toSet());
            String faltantes = ids.stream()
                    .filter(id -> !encontrados.contains(id))
                    .map(String::valueOf)
                    .sorted()
                    .collect(Collectors.joining(", "));
            throw new ExcepcionRecursoNoEncontrado(
                    "No existen los servicios: " + faltantes + ".");
        }

        for (Servicio servicio : servicios) {
            if (!servicio.isActivo()) {
                throw ExcepcionReglaNegocio.conflicto(
                        "INACTIVE_SERVICE",
                        "El servicio " + servicio.getNombre() + " no está activo y no se puede asignar.");
            }
        }

        return servicios;
    }

    private static EstilistaRespuesta respuestaDesde(Usuario usuario, List<Servicio> servicios) {
        List<ServicioRespuesta> asignados = servicios.stream()
                .sorted(Comparator.comparing(Servicio::getNombre, String.CASE_INSENSITIVE_ORDER))
                .map(ServicioRespuesta::desde)
                .toList();
        return new EstilistaRespuesta(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol(),
                asignados);
    }
}
