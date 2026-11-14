package peluqueria.controlador;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import peluqueria.modelo.Servicio;
import peluqueria.repositorio.RepositorioImagenServicio;
import peluqueria.repositorio.RepositorioServicio;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PruebasServicioRendimientoIntegration {

    @Autowired
    private RepositorioServicio repositorioServicio;

    @Autowired
    private RepositorioImagenServicio repositorioImagen;

    @LocalServerPort
    private int puerto;

    private final HttpClient clienteHttp = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @BeforeEach
    void limpiarServicios() {
        repositorioImagen.deleteAll();
        repositorioServicio.deleteAll();
    }

    @AfterEach
    void limpiarDespues() {
        repositorioImagen.deleteAll();
        repositorioServicio.deleteAll();
    }

    @Test
    void respondeEnMenosDeDosSegundosConMilServicios() throws Exception {
        List<Servicio> guardados = sembrarServicios(1_000);
        Long primerId = guardados.getFirst().getId();

        for (int calentamiento = 0; calentamiento < 3; calentamiento++) {
            enviar("/api/servicios");
            enviar("/api/servicios/" + primerId);
        }

        Metrica listado = medirHttp("/api/servicios", 30);
        Metrica detalle = medirHttp("/api/servicios/" + primerId, 30);

        imprimir("Listado por HTTP (1000 servicios)", listado);
        imprimir("Detalle por HTTP", detalle);

        assertThat(listado.maximoMs())
                .as("El listado debe responder en menos de 2 segundos (requisito no funcional)")
                .isLessThan(2_000);
        assertThat(listado.promedioMs())
                .as("El promedio del listado debe mantenerse holgadamente bajo 2 segundos")
                .isLessThan(1_000);
        assertThat(detalle.maximoMs())
                .as("El detalle debe responder en menos de 2 segundos")
                .isLessThan(2_000);
    }

    private HttpResponse<String> enviar(String ruta) throws Exception {
        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + puerto + ruta))
                .timeout(Duration.ofSeconds(5))
                .header("Accept", "application/json")
                .GET()
                .build();
        return clienteHttp.send(peticion, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private List<Servicio> sembrarServicios(int cantidad) {
        List<Servicio> servicios = new ArrayList<>(cantidad);
        for (int indice = 0; indice < cantidad; indice++) {
            Servicio servicio = new Servicio();
            servicio.setNombre("Servicio " + indice);
            servicio.setDescripcion("Descripción del servicio " + indice);
            servicio.setCosto(new BigDecimal("10.00"));
            servicio.setDuracionMinutos(30 + (indice % 60));
            servicio.setActivo(true);
            servicios.add(servicio);
        }
        return repositorioServicio.saveAll(servicios);
    }

    private Metrica medirHttp(String ruta, int repeticiones) throws Exception {
        long inicio = System.nanoTime();
        long maximo = 0;
        for (int repeticion = 0; repeticion < repeticiones; repeticion++) {
            long inicioConsulta = System.nanoTime();
            HttpResponse<String> respuesta = enviar(ruta);
            if (respuesta.statusCode() != 200) {
                throw new IllegalStateException("Respuesta inesperada: " + respuesta.statusCode());
            }
            maximo = Math.max(maximo, System.nanoTime() - inicioConsulta);
        }
        long total = System.nanoTime() - inicio;
        long promedioMs = (total / repeticiones) / 1_000_000;
        long maximoMs = maximo / 1_000_000;
        double peticionesPorSegundo = repeticiones / (total / 1_000_000_000.0);
        return new Metrica(repeticiones, promedioMs, maximoMs, peticionesPorSegundo);
    }

    private void imprimir(String nombre, Metrica metrica) {
        System.out.printf(Locale.ROOT,
                "[RENDIMIENTO] %s: %d peticiones, promedio %d ms, maximo %d ms, %.1f peticiones/s%n",
                nombre, metrica.repeticiones(), metrica.promedioMs(), metrica.maximoMs(),
                metrica.peticionesPorSegundo());
    }

    private record Metrica(int repeticiones, long promedioMs, long maximoMs, double peticionesPorSegundo) {
    }
}
