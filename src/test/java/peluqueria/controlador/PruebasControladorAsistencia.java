package peluqueria.controlador;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import peluqueria.excepcion.ExcepcionReglaNegocio;
import peluqueria.excepcion.ManejadorGlobalExcepciones;
import peluqueria.modelo.Estilista;
import peluqueria.seguridad.UsuarioActual;
import peluqueria.servicio.GestorAsistencia;
import peluqueria.transferencia.AsistenciaHoyRespuesta;
import peluqueria.transferencia.AsistenciaPersonalRespuesta;

/** Pruebas HTTP de asistencia, sin base de datos ni seguridad real. */
class PruebasControladorAsistencia {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 9);

    private GestorAsistencia gestorAsistencia;
    private MockMvc mockMvc;

    @BeforeEach
    void preparar() {
        gestorAsistencia = mock(GestorAsistencia.class);
        UsuarioActual usuarioActual = mock(UsuarioActual.class);

        Estilista personal = new Estilista();
        personal.setId(5L);
        personal.setEmail("marta@correo.com");
        when(usuarioActual.usuario()).thenReturn(personal);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new ControladorAsistencia(gestorAsistencia, usuarioActual))
                .setControllerAdvice(new ManejadorGlobalExcepciones())
                .build();
    }

    private static AsistenciaHoyRespuesta respuesta(boolean conEntrada, boolean conSalida) {
        return new AsistenciaHoyRespuesta(
                HOY,
                LocalTime.of(8, 0),
                LocalTime.of(8, 0),
                LocalTime.of(18, 0),
                conEntrada ? LocalTime.of(8, 0) : null,
                conSalida ? LocalTime.of(18, 0) : null,
                false,
                0,
                false,
                !conEntrada,
                conEntrada && !conSalida,
                OffsetDateTime.of(HOY.atTime(18, 0), ZoneOffset.ofHours(-4)));
    }

    @Test
    void listaLaAsistenciaDelDiaConNombreYHoras() throws Exception {
        when(gestorAsistencia.listarPorFecha(any())).thenReturn(List.of(
                new AsistenciaPersonalRespuesta(5L, "Marta", "marta@correo.com",
                        LocalTime.of(8, 0), LocalTime.of(17, 0), false, 0, true,
                        "Cita médica", true),
                new AsistenciaPersonalRespuesta(6L, "Luis", "luis@correo.com",
                        null, null, false, 0, false, null, false)));

        mockMvc.perform(get("/api/asistencia").param("fecha", "2026-11-09"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Marta"))
                .andExpect(jsonPath("$[0].horaEntrada").value("08:00:00"))
                .andExpect(jsonPath("$[0].salidaAnticipada").value(true))
                .andExpect(jsonPath("$[0].motivoSalida").value("Cita médica"))
                .andExpect(jsonPath("$[1].trabajo").value(false));

        verify(gestorAsistencia).listarPorFecha(LocalDate.of(2026, 11, 9));
    }

    @Test
    void devuelveElEstadoDelDia() throws Exception {
        when(gestorAsistencia.obtenerHoy(any())).thenReturn(respuesta(false, false));

        mockMvc.perform(get("/api/asistencia/hoy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.horaApertura").value("08:00:00"))
                .andExpect(jsonPath("$.horaCierre").value("18:00:00"))
                .andExpect(jsonPath("$.puedeRegistrarEntrada").value(true))
                .andExpect(jsonPath("$.puedeRegistrarSalida").value(false));
    }

    @Test
    void registraLaEntradaYResponde201() throws Exception {
        when(gestorAsistencia.registrarEntrada(any())).thenReturn(respuesta(true, false));

        mockMvc.perform(post("/api/asistencia/entrada"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.horaEntrada").value("08:00:00"));
    }

    @Test
    void registraLaSalidaConMotivo() throws Exception {
        when(gestorAsistencia.registrarSalida(any(), any())).thenReturn(respuesta(true, true));

        mockMvc.perform(post("/api/asistencia/salida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Cita médica\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.horaSalida").value("18:00:00"));

        verify(gestorAsistencia).registrarSalida(any(), org.mockito.ArgumentMatchers.eq("Cita médica"));
    }

    @Test
    void registraLaSalidaSinCuerpo() throws Exception {
        when(gestorAsistencia.registrarSalida(any(), any())).thenReturn(respuesta(true, true));

        mockMvc.perform(post("/api/asistencia/salida"))
                .andExpect(status().isOk());
    }

    @Test
    void responde409CuandoLaEntradaYaExiste() throws Exception {
        when(gestorAsistencia.registrarEntrada(any())).thenThrow(
                ExcepcionReglaNegocio.conflicto("ENTRADA_YA_REGISTRADA", "Ya registraste tu entrada de hoy."));

        mockMvc.perform(post("/api/asistencia/entrada"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("ENTRADA_YA_REGISTRADA"));
    }

    @Test
    void responde409CuandoLaEntradaEstaFueraDeHorario() throws Exception {
        when(gestorAsistencia.registrarEntrada(any())).thenThrow(
                ExcepcionReglaNegocio.conflicto("ENTRADA_FUERA_DE_HORARIO", "Ya cerró el local."));

        mockMvc.perform(post("/api/asistencia/entrada"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("ENTRADA_FUERA_DE_HORARIO"));
    }

    @Test
    void responde409CuandoLaSalidaNoTieneEntrada() throws Exception {
        when(gestorAsistencia.registrarSalida(any(), any())).thenThrow(
                ExcepcionReglaNegocio.conflicto("SIN_ENTRADA", "Primero debes registrar tu entrada."));

        mockMvc.perform(post("/api/asistencia/salida"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("SIN_ENTRADA"));
    }

    @Test
    void responde409CuandoLaSalidaYaExiste() throws Exception {
        when(gestorAsistencia.registrarSalida(any(), any())).thenThrow(
                ExcepcionReglaNegocio.conflicto("SALIDA_YA_REGISTRADA", "Ya registraste tu salida de hoy."));

        mockMvc.perform(post("/api/asistencia/salida"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("SALIDA_YA_REGISTRADA"));
    }

    @Test
    void responde400CuandoFaltaElMotivoAnticipado() throws Exception {
        when(gestorAsistencia.registrarSalida(any(), any())).thenThrow(
                ExcepcionReglaNegocio.invalida("MOTIVO_REQUERIDO", "Indica un motivo."));

        mockMvc.perform(post("/api/asistencia/salida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("MOTIVO_REQUERIDO"));
    }

    @Test
    void rechazaUnMotivoDeMasDeDoscientosCaracteres() throws Exception {
        mockMvc.perform(post("/api/asistencia/salida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"" + "a".repeat(201) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
}
