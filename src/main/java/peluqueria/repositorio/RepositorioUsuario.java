package peluqueria.repositorio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import peluqueria.modelo.Rol;
import peluqueria.modelo.Usuario;

/** Acceso a datos de cuentas. Lo usan autenticación, registro y gestión de personal. */
public interface RepositorioUsuario extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByRol(Rol rol);
}
