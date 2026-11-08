package com.ucab.peluqueria.repositorio;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ucab.peluqueria.modelo.Estilista;

/** Acceso a datos de estilistas. Lo usa GestorEstilistas. */
public interface RepositorioEstilista extends JpaRepository<Estilista, Long> {

    @EntityGraph(attributePaths = "servicios")
    List<Estilista> findByActivoTrueOrderByNombreAsc();

    /** Citas pendientes o confirmadas desde la fecha indicada: impiden desactivar al estilista. */
    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM citas c
                JOIN agendas a ON a.id = c.agenda_id
                WHERE a.estilista_id = :id
                  AND c.estado IN ('PENDIENTE', 'CONFIRMADA')
                  AND a.fecha >= :fecha
            )
            """, nativeQuery = true)
    boolean tieneCitasFuturasActivas(@Param("id") Long id, @Param("fecha") LocalDate fecha);
}
