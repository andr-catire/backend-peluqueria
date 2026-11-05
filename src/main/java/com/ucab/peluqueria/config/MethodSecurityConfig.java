package com.ucab.peluqueria.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Activa la seguridad por método para que @PreAuthorize funcione. La usa Spring Security al
 * procesar las peticiones.
 */
@Configuration
@EnableMethodSecurity
public class MethodSecurityConfig {
}
