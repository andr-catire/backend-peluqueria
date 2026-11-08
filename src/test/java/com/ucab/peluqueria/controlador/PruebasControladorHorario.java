package com.ucab.peluqueria.controlador;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.ucab.peluqueria.configuracion.PropiedadesHorario;
import com.ucab.peluqueria.excepcion.ExcepcionReglaNegocio;
import com.ucab.peluqueria.excepcion.ManejadorGlobalExcepciones;
import com.ucab.peluqueria.seguridad.UsuarioActual;
import com.ucab.peluqueria.servicio.GestorHorario;

/** Pruebas HTTP del horario editable, sin base de datos ni seguridad real. */
class PruebasControladorHorario {

    private static final ZoneId ZONA = ZoneId.of("America/Caracas");

    private GestorHorario gestorHorario;
    private MockMvc mockMvc;

    @BeforeEach
    void preparar() {
        gestorHorario = mock(GestorHorario.class);
        UsuarioActual usuarioActual = mock(UsuarioActual.class);
        when(usuarioActual.email()).thenReturn("admin@correo.com");

        mockMvc = MockMvcBuilders
                .standaloneSetup(new ControladorHorario(gestorHorario, usuarioActual))
                .setControllerAdvice(new ManejadorGlobalExcepciones())
                .build();
    }

    private static PropiedadesHorario horario(LocalTime apertura, LocalTime cierre) {
        return new PropiedadesHorario(ZONA, apertura, cierre, 0);
    }

    @Test
    void devuelveElHorarioVigente() throws Exception {
        when(gestorHorario.actual()).thenReturn(horario(LocalTime.of(8, 0), LocalTime.of(18, 0)));

        mockMvc.perform(get("/api/asistencia/horario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apertura").value("08:00:00"))
                .andExpect(jsonPath("$.cierre").value("18:00:00"))
                .andExpect(jsonPath("$.zona").value("America/Caracas"));
    }

    @Test
    void actualizaElHorario() throws Exception {
        when(gestorHorario.actualizar(any(), any(), any()))
                .thenReturn(horario(LocalTime.of(9, 0), LocalTime.of(20, 0)));

        mockMvc.perform(put("/api/asistencia/horario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"apertura":"09:00","cierre":"20:00"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apertura").value("09:00:00"))
                .andExpect(jsonPath("$.cierre").value("20:00:00"));
    }

    @Test
    void responde400SiLaAperturaNoEsAnteriorAlCierre() throws Exception {
        when(gestorHorario.actualizar(any(), any(), any())).thenThrow(
                ExcepcionReglaNegocio.invalida(
                        "HORARIO_INVALIDO", "La apertura debe ser anterior al cierre."));

        mockMvc.perform(put("/api/asistencia/horario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"apertura":"19:00","cierre":"18:00"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("HORARIO_INVALIDO"));
    }

    @Test
    void rechazaUnCuerpoIncompleto() throws Exception {
        mockMvc.perform(put("/api/asistencia/horario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"apertura":"09:00"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
}
