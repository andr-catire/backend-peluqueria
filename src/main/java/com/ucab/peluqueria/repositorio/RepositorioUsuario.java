package com.ucab.peluqueria.repositorio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ucab.peluqueria.modelo.Rol;
import com.ucab.peluqueria.modelo.Usuario;

public interface RepositorioUsuario extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByRol(Rol rol);

    /** Cambia el rol sin cargar ni modificar la entidad, para no chocar con la herencia JOINED. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Usuario u SET u.rol = :rol WHERE u.id = :id")
    int actualizarRol(@Param("id") Long id, @Param("rol") Rol rol);
}
