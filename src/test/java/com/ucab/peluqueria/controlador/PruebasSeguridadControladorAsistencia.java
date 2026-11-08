package com.ucab.peluqueria.controlador;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ucab.peluqueria.configuracion.ConfiguracionSeguridad;
import com.ucab.peluqueria.configuracion.ConfiguracionSeguridadMetodos;
import com.ucab.peluqueria.excepcion.ManejadorGlobalExcepciones;
import com.ucab.peluqueria.modelo.Estilista;
import com.ucab.peluqueria.repositorio.RepositorioUsuario;
import com.ucab.peluqueria.seguridad.FiltroAutenticacionJwt;
import com.ucab.peluqueria.seguridad.ServicioJwt;
import com.ucab.peluqueria.seguridad.UsuarioActual;
import com.ucab.peluqueria.servicio.GestorAsistencia;
import com.ucab.peluqueria.transferencia.AsistenciaHoyRespuesta;

/** Pruebas de acceso a la asistencia con la seguridad real configurada. */
@WebMvcTest(ControladorAsistencia.class)
@Import({
        ConfiguracionSeguridad.class,
        ConfiguracionSeguridadMetodos.class,
        ManejadorGlobalExcepciones.class,
        FiltroAutenticacionJwt.class
})
@TestPropertySource(properties = "app.frontend-url=http://localhost:5173")
class PruebasSeguridadControladorAsistencia {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 9);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GestorAsistencia gestorAsistencia;

    @MockitoBean
    private UsuarioActual usuarioActual;

    @MockitoBean
    private ServicioJwt servicioJwt;

    @MockitoBean
    private RepositorioUsuario repositorioUsuario;

    private static AsistenciaHoyRespuesta respuesta() {
        return new AsistenciaHoyRespuesta(
                HOY,
                LocalTime.of(8, 0),
                LocalTime.of(8, 0),
                LocalTime.of(18, 0),
                null,
                null,
                false,
                0,
                false,
                true,
                false,
                OffsetDateTime.of(HOY.atTime(18, 0), ZoneOffset.ofHours(-4)));
    }

    @Test
    void sinSesionResponde401() throws Exception {
        mockMvc.perform(get("/api/asistencia/hoy"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));

        mockMvc.perform(post("/api/asistencia/entrada"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/asistencia/salida"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void unAdministradorNoPuedeUsarLaAsistencia() throws Exception {
        mockMvc.perform(get("/api/asistencia/hoy"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/asistencia/entrada"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void unClienteNoPuedeUsarLaAsistencia() throws Exception {
        mockMvc.perform(get("/api/asistencia/hoy"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/asistencia/salida"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PERSONAL")
    void unEmpleadoConsultaYRegistraSuJornada() throws Exception {
        Estilista personal = new Estilista();
        personal.setId(5L);
        personal.setEmail("marta@correo.com");
        when(usuarioActual.usuario()).thenReturn(personal);
        when(gestorAsistencia.obtenerHoy(any())).thenReturn(respuesta());
        when(gestorAsistencia.registrarEntrada(any())).thenReturn(respuesta());
        when(gestorAsistencia.registrarSalida(any(), any())).thenReturn(respuesta());

        mockMvc.perform(get("/api/asistencia/hoy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.puedeRegistrarEntrada").value(true));

        mockMvc.perform(post("/api/asistencia/entrada"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/asistencia/salida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }
}
