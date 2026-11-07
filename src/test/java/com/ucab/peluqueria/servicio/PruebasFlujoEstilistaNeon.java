package com.ucab.peluqueria.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.ucab.peluqueria.modelo.Rol;
import com.ucab.peluqueria.modelo.Servicio;
import com.ucab.peluqueria.repositorio.RepositorioCliente;
import com.ucab.peluqueria.repositorio.RepositorioEstilista;
import com.ucab.peluqueria.repositorio.RepositorioServicio;
import com.ucab.peluqueria.repositorio.RepositorioUsuario;
import com.ucab.peluqueria.transferencia.EstilistaRequest;
import com.ucab.peluqueria.transferencia.EstilistaRespuesta;
import com.ucab.peluqueria.transferencia.RegistroRequest;
import com.ucab.peluqueria.transferencia.RegistroRespuesta;

/**
 * Prueba de integración del flujo completo contra Neon. Se ejecuta solo con RUN_DB_TESTS=true.
 * La transacción se revierte al terminar, así que no deja cuentas ni servicios de prueba.
 */
@SpringBootTest
@Transactional
@EnabledIfEnvironmentVariable(named = "RUN_DB_TESTS", matches = "true")
class PruebasFlujoEstilistaNeon {

    @Autowired
    private AuthService authService;

    @Autowired
    private EstilistaService estilistaService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RepositorioUsuario repositorioUsuario;

    @Autowired
    private RepositorioCliente repositorioCliente;

    @Autowired
    private RepositorioEstilista repositorioEstilista;

    @Autowired
    private RepositorioServicio repositorioServicio;

    @Test
    void registraPromueveConsultaElPerfilYDesvincula() {
        String sufijo = String.valueOf(Instant.now().toEpochMilli());
        String email = "prueba.flujo." + sufijo + "@correo.com";

        RegistroRespuesta registrado = authService.registrar(new RegistroRequest(
                "Prueba Flujo",
                email,
                "secreta123",
                "V-" + sufijo,
                "0414-" + sufijo));

        assertEquals(Rol.CLIENTE, estilistaService.buscarPorEmail(email).rol());
        assertTrue(repositorioCliente.existePerfil(registrado.id()));

        Servicio servicio = crearServicio("Servicio flujo " + sufijo);

        EstilistaRespuesta promovido = estilistaService.promoverAEmpleado(
                new EstilistaRequest(registrado.id(), Set.of(servicio.getId())));

        assertEquals(Rol.PERSONAL, promovido.rol());
        assertEquals(1, promovido.servicios().size());
        assertFalse(repositorioCliente.existePerfil(registrado.id()));
        assertTrue(repositorioEstilista.existePerfil(registrado.id()));

        EstilistaRespuesta perfil = estilistaService.obtenerMiPerfil(registrado.id());
        assertEquals(1, perfil.servicios().size());

        estilistaService.desvincularEstilista(registrado.id());

        assertEquals(Rol.CLIENTE, repositorioUsuario.findById(registrado.id()).orElseThrow().getRol());
        assertTrue(repositorioEstilista.buscarServicios(registrado.id()).isEmpty());
        assertTrue(repositorioEstilista.existePerfil(registrado.id()));
    }

    /**
     * Inserta el servicio de fixture directamente porque la base actual conserva una columna
     * heredada "precio NOT NULL" que Hibernate no elimina. Si la columna ya no existe, inserta
     * solo las columnas del modelo. La transacción de la prueba revierte la fila.
     */
    private Servicio crearServicio(String nombre) {
        boolean tienePrecio = Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                """
                SELECT EXISTS (
                    SELECT 1 FROM information_schema.columns
                    WHERE table_name = 'servicios' AND column_name = 'precio'
                )
                """,
                Boolean.class));

        BigDecimal costo = new BigDecimal("15.00");
        Long id;
        if (tienePrecio) {
            id = jdbcTemplate.queryForObject(
                    """
                    INSERT INTO servicios (nombre, descripcion, precio, costo, duracion_minutos, activo)
                    VALUES (?, ?, ?, ?, 30, true)
                    RETURNING id
                    """,
                    Long.class, nombre, "Servicio de la prueba de integración", costo, costo);
        } else {
            id = jdbcTemplate.queryForObject(
                    """
                    INSERT INTO servicios (nombre, descripcion, costo, duracion_minutos, activo)
                    VALUES (?, ?, ?, 30, true)
                    RETURNING id
                    """,
                    Long.class, nombre, "Servicio de la prueba de integración", costo);
        }

        return repositorioServicio.findById(id).orElseThrow();
    }
}
