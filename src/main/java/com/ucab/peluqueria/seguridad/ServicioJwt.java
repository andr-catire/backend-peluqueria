package com.ucab.peluqueria.seguridad;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ucab.peluqueria.modelo.Usuario;

/**
 * Servicio encargado de firmar y validar tokens JWT usando HMAC-SHA256 (RFC 7519).
 * Implementado enteramente en Java estándar para garantizar portabilidad y rendimiento.
 */
@Service
public class ServicioJwt {

    private static final Logger log = LoggerFactory.getLogger(ServicioJwt.class);
    private static final String ALGORITMO = "HmacSHA256";
    private final long duracionSegundos;

    // Header constante para JWT con HS256: {"alg":"HS256","typ":"JWT"}
    private static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private static final String HEADER_BASE64 = base64UrlEncode(HEADER_JSON.getBytes(StandardCharsets.UTF_8));

    private final byte[] secretKeyBytes;

    public ServicioJwt(
            @Value("${app.jwt.secret:}") String secret,
            @Value("${app.jwt.expiration:86400}") long duracionSegundos) {
        if (secret == null || secret.isBlank() || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "Configura JWT_SECRET con al menos 32 bytes antes de iniciar la aplicación.");
        }
        this.secretKeyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.duracionSegundos = duracionSegundos;
    }

    /**
     * Genera un token JWT firmado para el usuario especificado.
     */
    public String generarToken(Usuario usuario) {
        long ahora = Instant.now().getEpochSecond();
        long expira = ahora + duracionSegundos;

        try {
            String payloadJson = String.format(
                    "{\"sub\":\"%s\",\"rol\":\"%s\",\"nombre\":\"%s\",\"iat\":%d,\"exp\":%d}",
                    escaparJson(usuario.getEmail()),
                    escaparJson(usuario.getRol().name()),
                    escaparJson(usuario.getNombre() == null ? "" : usuario.getNombre()),
                    ahora,
                    expira
            );

            String payloadBase64 = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));
            String contenidoAFirmar = HEADER_BASE64 + "." + payloadBase64;
            String firmaBase64 = base64UrlEncode(calcularHmac(contenidoAFirmar));

            return contenidoAFirmar + "." + firmaBase64;
        } catch (Exception e) {
            log.error("Error al generar token JWT para el usuario {}", usuario.getEmail(), e);
            throw new IllegalStateException("No se pudo generar el token de autenticación", e);
        }
    }

    /**
     * Valida la firma del token y que no haya expirado.
     */
    public boolean validarToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        String[] partes = token.split("\\.");
        if (partes.length != 3) {
            return false;
        }

        try {
            String contenidoAFirmar = partes[0] + "." + partes[1];
            byte[] firmaCalculada = calcularHmac(contenidoAFirmar);
            byte[] firmaRecibida = Base64.getUrlDecoder().decode(partes[2]);

            if (!MessageDigest.isEqual(firmaCalculada, firmaRecibida)) {
                return false;
            }

            Long exp = extraerCampoNumero(partes[1], "exp");
            if (exp == null) {
                return false;
            }

            return exp > Instant.now().getEpochSecond();
        } catch (Exception e) {
            log.debug("Token JWT inválido: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extrae el email (subject) del token.
     */
    public String extraerEmail(String token) {
        String[] partes = separarPartes(token);
        return extraerCampoString(partes[1], "sub");
    }

    /**
     * Extrae el rol del usuario del token.
     */
    public String extraerRol(String token) {
        String[] partes = separarPartes(token);
        return extraerCampoString(partes[1], "rol");
    }

    private String[] separarPartes(String token) {
        if (token == null) {
            throw new IllegalArgumentException("El token no puede ser nulo");
        }
        String[] partes = token.split("\\.");
        if (partes.length != 3) {
            throw new IllegalArgumentException("Estructura de token JWT inválida");
        }
        return partes;
    }

    private String extraerPayloadJson(String payloadBase64) {
        byte[] bytes = Base64.getUrlDecoder().decode(payloadBase64);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private String extraerCampoString(String payloadBase64, String campo) {
        String json = extraerPayloadJson(payloadBase64);
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(campo) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private Long extraerCampoNumero(String payloadBase64, String campo) {
        String json = extraerPayloadJson(payloadBase64);
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(campo) + "\"\\s*:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        }
        return null;
    }

    private byte[] calcularHmac(String data) throws Exception {
        Mac mac = Mac.getInstance(ALGORITMO);
        mac.init(new SecretKeySpec(secretKeyBytes, ALGORITMO));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private static String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String escaparJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
