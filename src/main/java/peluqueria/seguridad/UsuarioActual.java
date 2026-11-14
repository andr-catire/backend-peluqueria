package peluqueria.seguridad;

import java.util.Locale;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import peluqueria.excepcion.ExcepcionRecursoNoEncontrado;
import peluqueria.modelo.Usuario;
import peluqueria.repositorio.RepositorioUsuario;

/**
 * Obtiene la cuenta autenticada desde el contexto de Spring Security (poblado por el filtro JWT
 * en la historia de inicio de sesión). Evita que el cliente envíe su propio id por la URL: el id
 * siempre sale del token.
 */
@Component
public class UsuarioActual {

    private final RepositorioUsuario repositorioUsuario;

    public UsuarioActual(RepositorioUsuario repositorioUsuario) {
        this.repositorioUsuario = repositorioUsuario;
    }

    /** Correo de la cuenta autenticada, tal como lo expone el token. */
    public String email() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null
                || !autenticacion.isAuthenticated()
                || autenticacion instanceof AnonymousAuthenticationToken) {
            throw new AuthenticationCredentialsNotFoundException("No hay una sesión activa.");
        }
        return autenticacion.getName();
    }

    /** Cuenta autenticada, recargada desde la base para tener datos y rol actualizados. */
    public Usuario usuario() {
        String correo = email().trim().toLowerCase(Locale.ROOT);
        return repositorioUsuario.findByEmailIgnoreCase(correo)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("No se encontró la cuenta autenticada."));
    }

    /** Identificador de la cuenta autenticada. */
    public Long id() {
        return usuario().getId();
    }
}
