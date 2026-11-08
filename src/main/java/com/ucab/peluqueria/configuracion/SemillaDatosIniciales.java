package com.ucab.peluqueria.configuracion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ucab.peluqueria.modelo.Rol;
import com.ucab.peluqueria.modelo.Servicio;
import com.ucab.peluqueria.modelo.Usuario;
import com.ucab.peluqueria.repositorio.RepositorioEstilista;
import com.ucab.peluqueria.repositorio.RepositorioServicio;
import com.ucab.peluqueria.repositorio.RepositorioUsuario;

@Component
@Order(2)
public class SemillaDatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SemillaDatosIniciales.class);

    private final RepositorioServicio repositorioServicio;
    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioEstilista repositorioEstilista;
    private final PasswordEncoder passwordEncoder;

    public SemillaDatosIniciales(
            RepositorioServicio repositorioServicio,
            RepositorioUsuario repositorioUsuario,
            RepositorioEstilista repositorioEstilista,
            PasswordEncoder passwordEncoder) {
        this.repositorioServicio = repositorioServicio;
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioEstilista = repositorioEstilista;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // 0. Asegurar cuenta de Administrador
        asegurarAdmin("Administrador Armandeus", "admin@peluqueria.com", "Password123*");

        // 1. Asegurar los 4 servicios
        List<Servicio> servicios = new ArrayList<>();
        servicios.add(asegurarServicio(
                "Corte y Estilo",
                "Corte profesional adaptado a tu estilo personal con acabado y estilizado.",
                new BigDecimal("25.00"),
                45));

        servicios.add(asegurarServicio(
                "Lavado y Secado Profesional",
                "Lavado profundo con productos premium y secado con moldeado.",
                new BigDecimal("15.00"),
                30));

        servicios.add(asegurarServicio(
                "Coloración y Tinte",
                "Aplicación de tinte y matizado con cobertura completa y brillo duradero.",
                new BigDecimal("50.00"),
                90));

        servicios.add(asegurarServicio(
                "Tratamiento de Hidratación Profunda",
                "Tratamiento nutritivo intensivo para restaurar la suavidad y el brillo del cabello.",
                new BigDecimal("35.00"),
                60));

        // 2. Asegurar los 2 estilistas
        Usuario carlos = asegurarEstilista("Carlos Gómez", "carlos.estilista@peluqueria.com", "Password123*", "0412-5550101");
        Usuario valeria = asegurarEstilista("Valeria Morales", "valeria.estilista@peluqueria.com", "Password123*", "0414-5550202");

        // 3. Asignar los 4 servicios a ambos estilistas
        for (Servicio s : servicios) {
            repositorioEstilista.asignarServicio(carlos.getId(), s.getId());
            repositorioEstilista.asignarServicio(valeria.getId(), s.getId());
        }

        log.info("Semilla de servicios y estilistas verificada correctamente.");
    }

    private Servicio asegurarServicio(String nombre, String descripcion, BigDecimal costo, int duracion) {
        return repositorioServicio.findByNombreIgnoreCase(nombre)
                .orElseGet(() -> {
                    Servicio nuevo = new Servicio();
                    nuevo.setNombre(nombre);
                    nuevo.setDescripcion(descripcion);
                    nuevo.setCosto(costo);
                    nuevo.setDuracionMinutos(duracion);
                    nuevo.setActivo(true);
                    return repositorioServicio.save(nuevo);
                });
    }

    private Usuario asegurarEstilista(String nombre, String email, String password, String telefono) {
        Usuario usuario = repositorioUsuario.findByEmailIgnoreCase(email.toLowerCase())
                .orElseGet(() -> {
                    Usuario u = new Usuario();
                    u.setNombre(nombre);
                    u.setEmail(email.toLowerCase());
                    u.setPassword(passwordEncoder.encode(password));
                    u.setRol(Rol.PERSONAL);
                    u.setActivo(true);
                    return repositorioUsuario.save(u);
                });

        repositorioEstilista.insertarPerfil(usuario.getId());
        return usuario;
    }

    private Usuario asegurarAdmin(String nombre, String email, String password) {
        return repositorioUsuario.findByEmailIgnoreCase(email.toLowerCase())
                .map(u -> {
                    u.setNombre(nombre);
                    u.setPassword(passwordEncoder.encode(password));
                    u.setRol(Rol.ADMIN);
                    u.setActivo(true);
                    return repositorioUsuario.save(u);
                })
                .orElseGet(() -> {
                    Usuario u = new Usuario();
                    u.setNombre(nombre);
                    u.setEmail(email.toLowerCase());
                    u.setPassword(passwordEncoder.encode(password));
                    u.setRol(Rol.ADMIN);
                    u.setActivo(true);
                    return repositorioUsuario.save(u);
                });
    }
}
