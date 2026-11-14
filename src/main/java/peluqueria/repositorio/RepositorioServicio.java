package peluqueria.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import peluqueria.modelo.Servicio;

/** Acceso a datos de servicios. Lo usa GestorServicios. */
public interface RepositorioServicio extends JpaRepository<Servicio, Long> {

    Optional<Servicio> findByNombreIgnoreCase(String nombre);

    List<Servicio> findByActivoTrueOrderByNombreAsc();
}
