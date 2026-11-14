package peluqueria.configuracion;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import peluqueria.modelo.Servicio;
import peluqueria.repositorio.RepositorioServicio;

/**
 * Crea los servicios de demostración al arrancar. No crea usuarios ni contraseñas:
 * el administrador sale de APP_ADMIN_EMAIL/APP_ADMIN_PASSWORD y el personal se registra
 * desde el panel.
 */
@Component
@Order(2)
@Profile("!test")
public class SemillaDatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SemillaDatosIniciales.class);

    private final RepositorioServicio repositorioServicio;

    public SemillaDatosIniciales(RepositorioServicio repositorioServicio) {
        this.repositorioServicio = repositorioServicio;
    }

    @Override
    @Transactional
    public void run(String... args) {
        asegurarServicio(
                "Corte y Estilo",
                "Corte profesional adaptado a tu estilo personal con acabado y estilizado.",
                new BigDecimal("25.00"),
                45);
        asegurarServicio(
                "Lavado y Secado Profesional",
                "Lavado profundo con productos premium y secado con moldeado.",
                new BigDecimal("15.00"),
                30);
        asegurarServicio(
                "Coloración y Tinte",
                "Aplicación de tinte y matizado con cobertura completa y brillo duradero.",
                new BigDecimal("50.00"),
                90);
        asegurarServicio(
                "Tratamiento de Hidratación Profunda",
                "Tratamiento nutritivo intensivo para restaurar la suavidad y el brillo del cabello.",
                new BigDecimal("35.00"),
                60);

        log.info("Semilla de servicios verificada correctamente.");
    }

    private Servicio asegurarServicio(String nombre, String descripcion, BigDecimal costo, int duracion) {
        return repositorioServicio.findByNombreIgnoreCase(nombre)
                .orElseGet(() -> {
                    Servicio nuevo = new Servicio();
                    nuevo.setNombre(nombre);
                    nuevo.setDescripcion(descripcion);
                    nuevo.setCosto(costo);
                    nuevo.setDuracionMinutos(duracion);
                    nuevo.setActivo(true);
                    return repositorioServicio.save(nuevo);
                });
    }
}
