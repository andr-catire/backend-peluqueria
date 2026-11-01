package com.ucab.peluqueria.config;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Seguridad de la API REST: sin sesión en el servidor, sin formulario de login y con CORS limitado
 * al frontend. De momento solo /api/health es público. Cuando llegue la historia de inicio de
 * sesión se agrega aquí el filtro JWT (carpeta security/) y las rutas públicas y por rol.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 12 rondas de BCrypt: encarece los ataques de diccionario y no se nota al iniciar sesión.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            // API sin cookies de sesión: la autenticación irá en el encabezado Authorization (JWT).
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/health").permitAll()
                .anyRequest().authenticated())
            .headers(headers -> headers
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000))
                .referrerPolicy(referrer -> referrer
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                .addHeaderWriter(new StaticHeadersWriter("Permissions-Policy",
                    "camera=(), microphone=(), geolocation=(), payment=()")))
            .exceptionHandling(handling -> handling
                // Sin credenciales válidas.
                .authenticationEntryPoint((request, response, exception) ->
                    responderJson(response, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED",
                        "Inicia sesión para continuar."))
                // Autenticado, pero sin el rol requerido.
                .accessDeniedHandler((request, response, exception) ->
                    responderJson(response, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN",
                        "No tienes permisos para realizar esta acción.")));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${app.frontend-url}") String frontendUrl) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(frontendUrl));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setMaxAge(Duration.ofHours(1));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    // Los textos son constantes del código (sin datos del usuario), por eso es seguro armar el JSON a mano.
    private static void responderJson(HttpServletResponse response, int status, String codigo, String mensaje)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"" + codigo + "\",\"message\":\"" + mensaje + "\"}");
    }
}
