package peluqueria.clientes;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a datos de clientes. Lo usan AuthService y GestorPerfil. */
public interface RepositorioCliente extends JpaRepository<Cliente, Long> {

    boolean existsByCedulaIgnoreCase(String cedula);

    Optional<Cliente> findByCedulaIgnoreCase(String cedula);
}
