package com.ucab.peluqueria.configuracion;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.ucab.peluqueria.modelo.Rol;
import com.ucab.peluqueria.modelo.Usuario;
import com.ucab.peluqueria.repositorio.RepositorioUsuario;

/**
 * Crea el primer administrador al arrancar, a partir de APP_ADMIN_EMAIL y APP_ADMIN_PASSWORD.
 * Solo actúa si todavía no existe ningún usuario con rol ADMIN, así que es seguro dejarlo siempre activo.
 */
@Component
public class AdminInicializador implements CommandLineRunner {

    static final int PASSWORD_MINIMO = 8;

    private static final Logger log = LoggerFactory.getLogger(AdminInicializador.class);

    private final RepositorioUsuario usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public AdminInicializador(
            RepositorioUsuario usuarioRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.email:}") String email,
            @Value("${app.admin.password:}") String password) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.existsByRol(Rol.ADMIN)) {
            return;
        }
        if (estaVacio(email) || estaVacio(password)) {
            log.warn("No existe ningún administrador y faltan APP_ADMIN_EMAIL o APP_ADMIN_PASSWORD: no se creó ninguno.");
            return;
        }

        String correo = email.trim().toLowerCase(Locale.ROOT);
        if (!correo.contains("@")) {
            throw new IllegalStateException("APP_ADMIN_EMAIL no es un correo válido.");
        }
        if (password.length() < PASSWORD_MINIMO) {
            throw new IllegalStateException("APP_ADMIN_PASSWORD debe tener al menos " + PASSWORD_MINIMO + " caracteres.");
        }

        Usuario admin = new Usuario();
        admin.setNombre("Administrador");
        admin.setEmail(correo);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRol(Rol.ADMIN);
        admin.setActivo(true);
        usuarioRepository.save(admin);
        log.info("Administrador inicial creado: {}", correo);
    }

    private static boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
