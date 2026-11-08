package com.ucab.peluqueria.repositorio;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ucab.peluqueria.modelo.Estilista;
import com.ucab.peluqueria.modelo.Servicio;

/** Acceso a datos de estilistas. */
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

    /** Servicios que atiende un estilista, ordenados por nombre. */
    @Query("SELECT s FROM Estilista e JOIN e.servicios s WHERE e.id = :id ORDER BY s.nombre")
    List<Servicio> buscarServicios(@Param("id") Long id);

    /** Estilistas activos que ofrecen un servicio específico. */
    @Query(value = """
            SELECT u.id, u.nombre, u.email
            FROM usuarios u
            JOIN personal p ON p.id = u.id
            JOIN personal_servicios ps ON ps.personal_id = p.id
            WHERE ps.servicio_id = :idServicio
              AND u.activo = true
            ORDER BY u.nombre
            """, nativeQuery = true)
    List<Object[]> buscarPorServicio(@Param("idServicio") Long idServicio);
}
