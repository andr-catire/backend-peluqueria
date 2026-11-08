package com.ucab.peluqueria.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ucab.peluqueria.modelo.HorarioLocal;

/** Acceso al horario único del local. Lo usa GestorHorario. */
public interface RepositorioHorario extends JpaRepository<HorarioLocal, Long> {
}
