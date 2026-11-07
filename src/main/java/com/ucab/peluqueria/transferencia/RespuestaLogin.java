package com.ucab.peluqueria.transferencia;

/**
 * Datos devueltos tras un inicio de sesión exitoso.
 * Incluye el token de autorización, el rol del usuario y datos de perfil.
 */
public record RespuestaLogin(
        String token,
        String rol,
        String nombre,
        String email
) {}
