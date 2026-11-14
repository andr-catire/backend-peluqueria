package peluqueria.repositorio;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import peluqueria.modelo.Agenda;
import peluqueria.modelo.Estilista;

public interface RepositorioAgenda extends JpaRepository<Agenda, Long> {

    Optional<Agenda> findByEstilistaAndFecha(Estilista estilista, LocalDate fecha);

    Optional<Agenda> findByEstilistaIdAndFecha(Long estilistaId, LocalDate fecha);
}
