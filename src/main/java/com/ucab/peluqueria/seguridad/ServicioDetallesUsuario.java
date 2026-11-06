package com.ucab.peluqueria.seguridad;

import java.util.Locale;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ucab.peluqueria.modelo.Usuario;
import com.ucab.peluqueria.repositorio.RepositorioUsuario;

/** Puente con Spring Security: carga la cuenta por correo y le asigna su rol (ROLE_ADMIN, ROLE_CLIENTE...). */
@Service
public class ServicioDetallesUsuario implements UserDetailsService {

    private final RepositorioUsuario usuarioRepository;

    public ServicioDetallesUsuario(RepositorioUsuario usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String correo = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));

        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword())
                .roles(usuario.getRol().name())   // agrega el prefijo ROLE_
                .disabled(!usuario.isActivo())
                .build();
    }
}
