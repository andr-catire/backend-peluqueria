package peluqueria.servicios;

import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso al contenido de las imágenes de servicios. Lo usa GestorImagenes. */
public interface RepositorioImagenServicio extends JpaRepository<ImagenServicio, Long> {
}
