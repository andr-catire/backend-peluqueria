package com.ucab.peluqueria.repositorio;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ucab.peluqueria.modelo.Asistencia;

/** Acceso a los registros diarios de asistencia. Lo usa GestorAsistencia. */
public interface RepositorioAsistencia extends JpaRepository<Asistencia, Long> {

    Optional<Asistencia> findByPersonalIdAndFecha(Long personalId, LocalDate fecha);

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
