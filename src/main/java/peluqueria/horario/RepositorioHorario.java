package peluqueria.horario;

import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso al horario único del local. Lo usa GestorHorario. */
public interface RepositorioHorario extends JpaRepository<HorarioLocal, Long> {
}
