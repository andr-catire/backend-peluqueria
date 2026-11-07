package com.ucab.peluqueria.seguridad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ucab.peluqueria.modelo.Rol;
import com.ucab.peluqueria.modelo.Usuario;

class PruebasServicioJwt {

    private ServicioJwt servicioJwt;

    @BeforeEach
    void preparar() {
        servicioJwt = new ServicioJwt("secreto_de_prueba_con_longitud_suficiente_para_hmac256", 86400);
    }

    @Test
    void generaYValidaTokenCorrectamente() {
        Usuario usuario = new Usuario();
        usuario.setEmail("admin@peluqueria.com");
        usuario.setNombre("Administrador");
        usuario.setRol(Rol.ADMIN);

        String token = servicioJwt.generarToken(usuario);

        assertNotNull(token);
        assertTrue(servicioJwt.validarToken(token));
        assertEquals("admin@peluqueria.com", servicioJwt.extraerEmail(token));
        assertEquals("ADMIN", servicioJwt.extraerRol(token));
    }

    @Test
    void rechazaTokensAlterados() {
        Usuario usuario = new Usuario();
        usuario.setEmail("admin@peluqueria.com");
        usuario.setNombre("Administrador");
        usuario.setRol(Rol.ADMIN);

        String token = servicioJwt.generarToken(usuario);
        String tokenAlterado = token + "xyz";

        assertFalse(servicioJwt.validarToken(tokenAlterado));
    }

    @Test
    void rechazaTokensNulosOVacios() {
        assertFalse(servicioJwt.validarToken(null));
        assertFalse(servicioJwt.validarToken(""));
        assertFalse(servicioJwt.validarToken("token-invalido-sin-partes"));
    }
}
