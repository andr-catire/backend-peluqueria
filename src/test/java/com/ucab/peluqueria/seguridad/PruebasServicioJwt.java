package com.ucab.peluqueria.seguridad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import com.ucab.peluqueria.modelo.Rol;
import com.ucab.peluqueria.modelo.Usuario;

class PruebasServicioJwt {

    private static final String SECRETO = "secreto_de_prueba_con_longitud_suficiente_para_hmac256";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ServicioJwt servicioJwt;

    @BeforeEach
    void preparar() {
        servicioJwt = new ServicioJwt(SECRETO, 86400);
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

        assertFalse(servicioJwt.validarToken(token + "xyz"));
    }

    @Test
    void rechazaTokensNulosVaciosODeDosPartes() {
        assertFalse(servicioJwt.validarToken(null));
        assertFalse(servicioJwt.validarToken(""));
        assertFalse(servicioJwt.validarToken("token-invalido-sin-partes"));
        assertFalse(servicioJwt.validarToken("solo.dos"));
    }

    @Test
    void rechazaTokenVencido() throws Exception {
        long expirado = Instant.now().getEpochSecond() - 60;
        String token = firmar("""
                {"sub":"admin@peluqueria.com","rol":"ADMIN","exp":%d}
                """.formatted(expirado));

        assertFalse(servicioJwt.validarToken(token));
    }

    @Test
    void rechazaTokenSinExpiracion() throws Exception {
        String token = firmar("""
                {"sub":"admin@peluqueria.com","rol":"ADMIN","iat":1700000000}
                """);

        assertFalse(servicioJwt.validarToken(token));
    }

    @Test
    void elNombreMaliciosoNoAlteraElCorreoNiLaExpiracion() throws Exception {
        String nombreMalicioso = "x\\\"sub\\\":\\\"x\\\",\\\"exp\\\":9999999999\\\\";
        long expirado = Instant.now().getEpochSecond() - 60;

        ObjectNode payload = MAPPER.createObjectNode();
        payload.put("sub", "real@correo.com");
        payload.put("rol", "ADMIN");
        payload.put("nombre", nombreMalicioso);
        payload.put("exp", expirado);

        String token = firmar(MAPPER.writeValueAsString(payload));

        assertEquals("real@correo.com", servicioJwt.extraerEmail(token));
        assertEquals("ADMIN", servicioJwt.extraerRol(token));
        assertFalse(servicioJwt.validarToken(token));
    }

    private static String firmar(String payloadJson) throws Exception {
        String header = base64("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = base64(payloadJson);
        String contenido = header + "." + payload;

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRETO.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return contenido + "." + base64(mac.doFinal(contenido.getBytes(StandardCharsets.UTF_8)));
    }

    private static String base64(String texto) {
        return base64(texto.getBytes(StandardCharsets.UTF_8));
    }

    private static String base64(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

