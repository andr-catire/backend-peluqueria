package com.ucab.peluqueria.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ucab.peluqueria.modelo.Cliente;

/** Acceso a datos de clientes. Lo usa AuthService al registrar una cuenta. */
public interface RepositorioCliente extends JpaRepository<Cliente, Long> {

    boolean existsByCedulaIgnoreCase(String cedula);
}
