package peluqueria.repositorio;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import peluqueria.modelo.Asistencia;

/** Acceso a los registros diarios de asistencia. Lo usa GestorAsistencia. */
public interface RepositorioAsistencia extends JpaRepository<Asistencia, Long> {

    Optional<Asistencia> findByPersonalIdAndFecha(Long personalId, LocalDate fecha);

    /** Todas las asistencias de una fecha, con el estilista ya cargado. */
    @EntityGraph(attributePaths = "personal")
    List<Asistencia> findByFecha(LocalDate fecha);

    /**
     * Registra la salida solo si aún no existe, para que un segundo envío no sobrescriba la hora.
     * Devuelve cuántas filas se actualizaron.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Asistencia a
               SET a.horaSalida = :horaSalida,
                   a.salidaAnticipada = :anticipada,
                   a.motivoSalida = :motivo
             WHERE a.id = :id AND a.horaSalida IS NULL
            """)
    int registrarSalida(
            @Param("id") Long id,
            @Param("horaSalida") LocalTime horaSalida,
            @Param("anticipada") boolean anticipada,
            @Param("motivo") String motivo);
}
