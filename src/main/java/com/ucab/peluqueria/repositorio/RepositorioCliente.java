package com.ucab.peluqueria.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ucab.peluqueria.modelo.Cliente;

/** Acceso a datos de clientes. Lo usan AuthService y EstilistaService. */
public interface RepositorioCliente extends JpaRepository<Cliente, Long> {

    boolean existsByCedulaIgnoreCase(String cedula);

    /** Indica si la cuenta tiene ficha de cliente. */
    @Query(value = "SELECT EXISTS (SELECT 1 FROM clientes WHERE id = :id)", nativeQuery = true)
    boolean existePerfil(@Param("id") Long id);

    /** Una ficha de cliente con citas no se puede borrar: la FK de citas la referencia. */
    @Query(value = "SELECT EXISTS (SELECT 1 FROM citas WHERE cliente_id = :id)", nativeQuery = true)
    boolean tieneCitas(@Param("id") Long id);

    /**
     * Elimina la ficha de cliente al promover la cuenta a estilista, para que no queden dos
     * perfiles (Cliente y Estilista) sobre el mismo usuario. Solo se llama si no tiene citas.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM clientes WHERE id = :id", nativeQuery = true)
    void eliminarPerfil(@Param("id") Long id);
}
