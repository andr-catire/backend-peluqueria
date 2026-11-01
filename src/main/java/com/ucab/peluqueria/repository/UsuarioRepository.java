package com.ucab.peluqueria.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ucab.peluqueria.model.Rol;
import com.ucab.peluqueria.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByRol(Rol rol);
}
