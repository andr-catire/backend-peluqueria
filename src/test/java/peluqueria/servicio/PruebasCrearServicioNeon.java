package peluqueria.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import peluqueria.modelo.Servicio;
import peluqueria.repositorio.RepositorioServicio;
import peluqueria.transferencia.ServicioRespuesta;
import peluqueria.transferencia.SolicitudServicio;

/**
 * Prueba de integración que crea un servicio real en Neon. Se ejecuta solo con
 * RUN_DB_TESTS=true y revierte la transacción al terminar.
 */
@SpringBootTest
@Transactional
@EnabledIfEnvironmentVariable(named = "RUN_DB_TESTS", matches = "true")
class PruebasCrearServicioNeon {

    @Autowired
    private GestorServicios gestorServicios;

    @Autowired
    private RepositorioServicio repositorioServicio;

    @Test
    void creaUnServicioEnLaBaseDeDatos() {
        String nombre = "Servicio de prueba " + Instant.now().toEpochMilli();

        ServicioRespuesta respuesta = gestorServicios.crear(new SolicitudServicio(
                nombre,
                new BigDecimal("10.00"),
                new BigDecimal("30"),
                "Servicio creado desde una prueba de integración"));

        assertNotNull(respuesta.id());
        assertEquals(nombre, respuesta.nombre());
        assertEquals("Servicio creado desde una prueba de integración", respuesta.descripcion());

        Servicio guardado = repositorioServicio.findByNombreIgnoreCase(nombre).orElseThrow();
        assertEquals(30, guardado.getDuracionMinutos());
        assertTrue(guardado.isActivo());
    }
}
