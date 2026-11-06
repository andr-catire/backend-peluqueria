package com.ucab.peluqueria;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Arranca toda la aplicación, así que necesita una base PostgreSQL real (por ejemplo la rama
 * "development" de Neon). Se activa cuando el equipo configure una base de pruebas.
 */
@Disabled("Requiere una base de datos PostgreSQL de pruebas")
@SpringBootTest
class PruebasAplicacionPeluqueria {

    @Test
    void contextLoads() {
    }
}
