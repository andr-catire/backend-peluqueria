package com.ucab.peluqueria.seguridad;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ucab.peluqueria.modelo.Usuario;
import com.ucab.peluqueria.repositorio.RepositorioUsuario;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro que intercepta cada petición HTTP, valida el Bearer y establece la autenticación
 * con el rol actual del usuario en base de datos.
 */
@Component
public class FiltroAutenticacionJwt extends OncePerRequestFilter {

    private static final String PREFIJO_BEARER = "Bearer ";
    private final ServicioJwt servicioJwt;
    private final RepositorioUsuario repositorioUsuario;

    public FiltroAutenticacionJwt(ServicioJwt servicioJwt, RepositorioUsuario repositorioUsuario) {
        this.servicioJwt = servicioJwt;
        this.repositorioUsuario = repositorioUsuario;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String encabezado = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (encabezado != null && encabezado.startsWith(PREFIJO_BEARER)) {
            String token = encabezado.substring(PREFIJO_BEARER.length()).trim();

            if (servicioJwt.validarToken(token)) {
                String email = servicioJwt.extraerEmail(token);

                if (email != null) {
                    Usuario usuario = repositorioUsuario.findByEmailIgnoreCase(email).orElse(null);

                    if (usuario != null && usuario.isActivo() && usuario.getRol() != null) {
                        SimpleGrantedAuthority authority =
                                new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name());
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(email, null, List.of(authority));

                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
