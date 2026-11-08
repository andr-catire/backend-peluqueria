package com.ucab.peluqueria.repositorio;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ucab.peluqueria.modelo.Cita;
import com.ucab.peluqueria.modelo.EstadoCita;

public interface RepositorioCita extends JpaRepository<Cita, Long> {

    @Query("""
            SELECT c FROM Cita c
            JOIN c.agenda a
            WHERE a.estilista.id = :estilistaId
              AND a.fecha = :fecha
              AND c.estado <> :cancelada
            ORDER BY c.horaInicio ASC
            """)
    List<Cita> buscarPorEstilistaYFecha(
            @Param("estilistaId") Long estilistaId,
            @Param("fecha") LocalDate fecha,
            @Param("cancelada") EstadoCita cancelada);

    @Query("""
            SELECT c FROM Cita c
            JOIN c.agenda a
            WHERE c.cliente.id = :clienteId
              AND a.fecha = :fecha
              AND c.estado <> :cancelada
            ORDER BY c.horaInicio ASC
            """)
    List<Cita> buscarPorClienteYFecha(
            @Param("clienteId") Long clienteId,
            @Param("fecha") LocalDate fecha,
            @Param("cancelada") EstadoCita cancelada);

    @Query("""
            SELECT c FROM Cita c
            JOIN FETCH c.agenda a
            JOIN FETCH a.estilista e
            JOIN FETCH c.servicio s
            WHERE c.cliente.id = :clienteId
            ORDER BY a.fecha DESC, c.horaInicio DESC
            """)
    List<Cita> buscarPorCliente(@Param("clienteId") Long clienteId);

    @Query("""
            SELECT c FROM Cita c
            JOIN FETCH c.agenda a
            JOIN FETCH a.estilista e
            JOIN FETCH c.cliente cl
            JOIN FETCH c.servicio s
            WHERE a.fecha BETWEEN :desde AND :hasta
              AND c.estado <> :cancelada
            ORDER BY a.fecha ASC, c.horaInicio ASC
            """)
    List<Cita> buscarSemanal(
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta,
            @Param("cancelada") EstadoCita cancelada);

    @Query("""
            SELECT c FROM Cita c
            JOIN FETCH c.agenda a
            JOIN FETCH a.estilista e
            JOIN FETCH c.cliente cl
            JOIN FETCH c.servicio s
            WHERE a.estilista.id = :estilistaId
              AND a.fecha BETWEEN :desde AND :hasta
              AND c.estado <> :cancelada
            ORDER BY a.fecha ASC, c.horaInicio ASC
            """)
    List<Cita> buscarSemanalPorEstilista(
            @Param("estilistaId") Long estilistaId,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta,
            @Param("cancelada") EstadoCita cancelada);
}
