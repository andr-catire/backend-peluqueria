package com.ucab.peluqueria.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.ucab.peluqueria.modelo.Estilista;
import com.ucab.peluqueria.modelo.Servicio;

/**
 * Acceso a datos de estilistas (tabla personal). Lo usa EstilistaService.
 */
public interface RepositorioEstilista extends Repository<Estilista, Long> {

    /**
     * Crea la fila de la subtabla personal para un usuario que se promociona.
     */
    @Modifying
    @Query(value = "INSERT INTO personal (id) VALUES (:id) ON CONFLICT (id) DO NOTHING", nativeQuery = true)
    void insertarPerfil(@Param("id") Long id);

    /** Indica si la cuenta tiene fila en personal, es decir, si fue promovida a estilista. */
    @Query(value = "SELECT EXISTS (SELECT 1 FROM personal WHERE id = :id)", nativeQuery = true)
    boolean existePerfil(@Param("id") Long id);

    /** Asigna un servicio al estilista sin duplicar la relación si ya estaba asignada. */
    @Modifying
    @Query(value = """
            INSERT INTO personal_servicios (personal_id, servicio_id)
            VALUES (:idEstilista, :idServicio)
            ON CONFLICT (personal_id, servicio_id) DO NOTHING
            """, nativeQuery = true)
    void asignarServicio(@Param("idEstilista") Long idEstilista, @Param("idServicio") Long idServicio);

    /** Servicios asignados al estilista, ordenados por nombre. */
    @Query(value = """
            SELECT s.*
            FROM servicios s
            JOIN personal_servicios ps ON ps.servicio_id = s.id
            WHERE ps.personal_id = :id
            ORDER BY lower(s.nombre)
            """, nativeQuery = true)
    List<Servicio> buscarServicios(@Param("id") Long id);

    /** Citas pendientes o confirmadas de hoy en adelante: impiden desvincular al estilista. */
    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM citas c
                JOIN agendas a ON a.id = c.agenda_id
                WHERE a.estilista_id = :id
                  AND c.estado IN ('PENDIENTE', 'CONFIRMADA')
                  AND a.fecha >= CURRENT_DATE
            )
            """, nativeQuery = true)
    boolean tieneCitasFuturasActivas(@Param("id") Long id);

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
