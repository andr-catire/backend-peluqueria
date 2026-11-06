package com.ucab.peluqueria.repositorio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ucab.peluqueria.modelo.Rol;
import com.ucab.peluqueria.modelo.Usuario;

public interface RepositorioUsuario extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByRol(Rol rol);
}
