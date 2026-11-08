package com.ucab.peluqueria.repositorio;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ucab.peluqueria.modelo.Estilista;

/** Acceso a datos de estilistas. */
public interface RepositorioEstilista extends JpaRepository<Estilista, Long> {

    /**
     * Crea la fila de la subtabla personal para un usuario que se promociona.
     */
    @Modifying
    @Query(value = "INSERT INTO personal (id) VALUES (:id) ON CONFLICT (id) DO NOTHING", nativeQuery = true)
    void insertarPerfil(@Param("id") Long id);

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

    /** Vacía las asignaciones de servicios del estilista sin tocar su fila personal. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM personal_servicios WHERE personal_id = :id", nativeQuery = true)
    void eliminarServicios(@Param("id") Long id);

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