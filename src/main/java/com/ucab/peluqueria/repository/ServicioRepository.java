package com.ucab.peluqueria.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ucab.peluqueria.model.Servicio;

/** Acceso a datos de servicios. Lo usa ServicioService. */
public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    boolean existsByNombreIgnoreCaseAndActivoTrue(String nombre);
}
