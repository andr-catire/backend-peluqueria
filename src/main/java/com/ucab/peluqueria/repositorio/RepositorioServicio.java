package com.ucab.peluqueria.repositorio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ucab.peluqueria.modelo.Servicio;

/** Acceso a datos de servicios. Lo usa GestorServicios. */
public interface RepositorioServicio extends JpaRepository<Servicio, Long> {

    Optional<Servicio> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);
}
