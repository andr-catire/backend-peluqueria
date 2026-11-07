package com.ucab.peluqueria.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ucab.peluqueria.modelo.ImagenServicio;

/** Acceso al contenido de las imágenes de servicios. Lo usa GestorImagenes. */
public interface RepositorioImagenServicio extends JpaRepository<ImagenServicio, Long> {
}
