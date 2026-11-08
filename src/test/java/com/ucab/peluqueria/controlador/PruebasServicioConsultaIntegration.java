package com.ucab.peluqueria.controlador;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.ucab.peluqueria.modelo.ImagenServicio;
import com.ucab.peluqueria.modelo.Servicio;
import com.ucab.peluqueria.repositorio.RepositorioImagenServicio;
import com.ucab.peluqueria.repositorio.RepositorioServicio;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PruebasServicioConsultaIntegration {

    private static final String PNG_UNO_POR_UNO = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk"
            + "YAAAAAYAAjCB0C8AAAAASUVORK5CYII=";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RepositorioServicio repositorioServicio;

    @Autowired
    private RepositorioImagenServicio repositorioImagen;

    private Servicio corte;

    @BeforeEach
    void prepararServicios() {
        repositorioImagen.deleteAll();
        repositorioServicio.deleteAll();

        corte = guardar("Corte de cabello", "25.50", 45, true);
        guardar("Peinado", "12.00", 30, false);
    }

    @Test
    void listaSoloServiciosActivosConPrecioEImagenUrl() throws Exception {
        mockMvc.perform(get("/api/servicios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Corte de cabello"))
                .andExpect(jsonPath("$[0].precio").value(25.50))
                .andExpect(jsonPath("$[0].imagenUrl").doesNotExist());
    }

    @Test
    void consultaDetalleDeServicioActivo() throws Exception {
        mockMvc.perform(get("/api/servicios/{id}", corte.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(corte.getId()))
                .andExpect(jsonPath("$.nombre").value("Corte de cabello"))
                .andExpect(jsonPath("$.precio").value(25.50));
    }

    @Test
    void responde404SiElServicioNoExisteOEstaInactivo() throws Exception {
        Servicio inactivo = repositorioServicio.findByNombreIgnoreCase("Peinado").orElseThrow();

        mockMvc.perform(get("/api/servicios/{id}", inactivo.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));

        mockMvc.perform(get("/api/servicios/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void creaServicioConPrecioDesdeLaApi() throws Exception {
        String cuerpo = """
                {"nombre":"Barba","precio":8.75,"duracionMinutos":20,"descripcion":"Perfilado"}
                """;

        mockMvc.perform(post("/api/servicios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Barba"))
                .andExpect(jsonPath("$.precio").value(8.75));

        Servicio guardado = repositorioServicio.findByNombreIgnoreCase("Barba").orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(0,
                new BigDecimal("8.75").compareTo(guardado.getCosto()));
    }

    @Test
    void sirveLaImagenPublicaDelServicio() throws Exception {
        byte[] bytes = Base64.getDecoder().decode(PNG_UNO_POR_UNO);
        ImagenServicio imagen = new ImagenServicio();
        imagen.setId(corte.getId());
        imagen.setTipo("image/png");
        imagen.setDatos(bytes);
        repositorioImagen.save(imagen);
        corte.setTieneImagen(true);
        repositorioServicio.save(corte);

        mockMvc.perform(get("/api/servicios/{id}/imagen", corte.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(bytes));
    }

    private Servicio guardar(String nombre, String precio, int duracion, boolean activo) {
        Servicio servicio = new Servicio();
        servicio.setNombre(nombre);
        servicio.setDescripcion("Descripción de " + nombre);
        servicio.setCosto(new BigDecimal(precio));
        servicio.setDuracionMinutos(duracion);
        servicio.setActivo(activo);
        return repositorioServicio.save(servicio);
    }
}
