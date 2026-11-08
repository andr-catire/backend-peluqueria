package com.ucab.peluqueria.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

import com.ucab.peluqueria.configuracion.PropiedadesHorario;
import com.ucab.peluqueria.excepcion.ExcepcionReglaNegocio;
import com.ucab.peluqueria.modelo.Asistencia;
import com.ucab.peluqueria.modelo.Estilista;
import com.ucab.peluqueria.repositorio.RepositorioAsistencia;
import com.ucab.peluqueria.repositorio.RepositorioEstilista;
import com.ucab.peluqueria.transferencia.AsistenciaHoyRespuesta;
import com.ucab.peluqueria.transferencia.AsistenciaPersonalRespuesta;

/** Pruebas de las reglas de entrada y salida con un reloj fijo. */
class PruebasGestorAsistencia {

    private static final ZoneId ZONA = ZoneId.of("America/Caracas");
    private static final LocalDate HOY = LocalDate.of(2026, 11, 9);

    private RepositorioAsistencia repositorio;
    private RepositorioEstilista repositorioEstilista;
    private GestorHorario gestorHorario;
    private Estilista personal;

    @BeforeEach
    void preparar() {
        repositorio = mock(RepositorioAsistencia.class);
        repositorioEstilista = mock(RepositorioEstilista.class);
        gestorHorario = mock(GestorHorario.class);
        when(gestorHorario.actual()).thenReturn(horario(0));
        personal = new Estilista();
        personal.setId(5L);
        personal.setNombre("Marta");
        personal.setEmail("marta@correo.com");

        when(repositorio.saveAndFlush(any(Asistencia.class))).thenAnswer(invocacion -> {
            Asistencia guardada = invocacion.getArgument(0);
            guardada.setId(20L);
            return guardada;
        });
    }

    private static Clock reloj(int hora, int minuto) {
        return Clock.fixed(HOY.atTime(hora, minuto).atZone(ZONA).toInstant(), ZONA);
    }

    private GestorAsistencia gestor(Clock reloj) {
        return new GestorAsistencia(repositorio, repositorioEstilista, gestorHorario, reloj);
    }

    private static PropiedadesHorario horario(int tolerancia) {
        return new PropiedadesHorario(
                ZONA, LocalTime.of(8, 0), LocalTime.of(18, 0), tolerancia);
    }

    private static Asistencia conEntrada() {
        Asistencia asistencia = new Asistencia();
        asistencia.setId(20L);
        asistencia.setFecha(HOY);
        asistencia.setHoraEntrada(LocalTime.of(8, 0));
        return asistencia;
    }

    private static Asistencia conSalida(LocalTime hora, boolean anticipada, String motivo) {
        Asistencia asistencia = conEntrada();
        asistencia.setHoraSalida(hora);
        asistencia.setSalidaAnticipada(anticipada);
        asistencia.setMotivoSalida(motivo);
        return asistencia;
    }

    @Test
    void registraUnaEntradaPuntualALasOcho() {
        when(repositorio.findByPersonalIdAndFecha(5L, HOY)).thenReturn(Optional.empty());

        AsistenciaHoyRespuesta respuesta = gestor(reloj(8, 0)).registrarEntrada(personal);

        assertEquals(LocalTime.of(8, 0), respuesta.horaEntrada());
        assertFalse(respuesta.llegoTarde());
        assertEquals(0, respuesta.minutosTarde());
        assertFalse(respuesta.puedeRegistrarEntrada());
        assertFalse(respuesta.puedeRegistrarSalida());
    }

    @Test
    void antesDeLaAperturaLaEntradaEsPuntual() {
        when(repositorio.findByPersonalIdAndFecha(5L, HOY)).thenReturn(Optional.empty());

        AsistenciaHoyRespuesta respuesta = gestor(reloj(7, 30)).registrarEntrada(personal);

        assertFalse(respuesta.llegoTarde());
        assertEquals(0, respuesta.minutosTarde());
    }

    @Test
    void marcaDoceMinutosDeRetrasoALasOchoYDoce() {
        when(repositorio.findByPersonalIdAndFecha(5L, HOY)).thenReturn(Optional.empty());

        AsistenciaHoyRespuesta respuesta = gestor(reloj(8, 12)).registrarEntrada(personal);

        assertTrue(respuesta.llegoTarde());
        assertEquals(12, respuesta.minutosTarde());
    }

    @Test
    void laToleranciaEvitaElRetrasoHastaSuLimite() {
        when(repositorio.findByPersonalIdAndFecha(5L, HOY)).thenReturn(Optional.empty());
        when(gestorHorario.actual()).thenReturn(horario(15));
        GestorAsistencia conTolerancia = new GestorAsistencia(
                repositorio, repositorioEstilista, gestorHorario, reloj(8, 12));

        assertFalse(conTolerancia.registrarEntrada(personal).llegoTarde());
    }

    @Test
    void pasadoElLimiteDeToleranciaCuentaDesdeLaApertura() {
        when(repositorio.findByPersonalIdAndFecha(5L, HOY)).thenReturn(Optional.empty());
        when(gestorHorario.actual()).thenReturn(horario(15));
        GestorAsistencia conTolerancia = new GestorAsistencia(
                repositorio, repositorioEstilista, gestorHorario, reloj(8, 20));

        AsistenciaHoyRespuesta respuesta = conTolerancia.registrarEntrada(personal);

        assertTrue(respuesta.llegoTarde());
        assertEquals(20, respuesta.minutosTarde());
    }

    @Test
    void noRegistraEntradaDespuesDelCierre() {
        when(repositorio.findByPersonalIdAndFecha(5L, HOY)).thenReturn(Optional.empty());

        ExcepcionReglaNegocio excepcion = assertThrows(ExcepcionReglaNegocio.class,
                () -> gestor(reloj(18, 1)).registrarEntrada(personal));

        assertEquals("ENTRADA_FUERA_DE_HORARIO", excepcion.getCodigo());
        assertEquals(HttpStatus.CONFLICT, excepcion.getStatus());
        verify(repositorio, never()).saveAndFlush(any());
    }

    @Test
    void noRegistraUnaSegundaEntradaElMismoDia() {
        when(repositorio.findByPersonalIdAndFecha(5L, HOY))
                .thenReturn(Optional.of(conEntrada()));

        ExcepcionReglaNegocio excepcion = assertThrows(ExcepcionReglaNegocio.class,
                () -> gestor(reloj(8, 30)).registrarEntrada(personal));

        assertEquals("ENTRADA_YA_REGISTRADA", excepcion.getCodigo());
        verify(repositorio, never()).saveAndFlush(any());
    }

    @Test
    void traduceLaViolacionDelIndiceUnicoAEntradaYaRegistrada() {
        when(repositorio.findByPersonalIdAndFecha(5L, HOY)).thenReturn(Optional.empty());
        when(repositorio.saveAndFlush(any(Asistencia.class))).thenThrow(
                new DataIntegrityViolationException("duplicada",
                        new SQLException("duplicada", "23505")));

        ExcepcionReglaNegocio excepcion = assertThrows(ExcepcionReglaNegocio.class,
                () -> gestor(reloj(8, 0)).registrarEntrada(personal));

        assertEquals("ENTRADA_YA_REGISTRADA", excepcion.getCodigo());
    }

    @Test
    void noRegistraSalidaSinEntrada() {
        when(repositorio.findByPersonalIdAndFecha(5L, HOY)).thenReturn(Optional.empty());

        ExcepcionReglaNegocio excepcion = assertThrows(ExcepcionReglaNegocio.class,
                () -> gestor(reloj(18, 0)).registrarSalida(personal, null));

        assertEquals("SIN_ENTRADA", excepcion.getCodigo());
        assertEquals(HttpStatus.CONFLICT, excepcion.getStatus());
    }

    @Test
    void exigeMotivoParaSalirAntesDelCierre() {
        when(repositorio.findByPersonalIdAndFecha(5L, HOY))
                .thenReturn(Optional.of(conEntrada()));

        ExcepcionReglaNegocio excepcion = assertThrows(ExcepcionReglaNegocio.class,
                () -> gestor(reloj(17, 0)).registrarSalida(personal, null));

        assertEquals("MOTIVO_REQUERIDO", excepcion.getCodigo());
        assertEquals(HttpStatus.BAD_REQUEST, excepcion.getStatus());
        verify(repositorio, never()).registrarSalida(anyLong(), any(), anyBoolean(), any());
    }

    @Test
    void registraUnaSalidaAnticipadaConMotivo() {
        when(repositorio.findByPersonalIdAndFecha(5L, HOY)).thenReturn(
                Optional.of(conEntrada()),
                Optional.of(conSalida(LocalTime.of(17, 0), true, "Cita médica")));
        when(repositorio.registrarSalida(20L, LocalTime.of(17, 0), true, "Cita médica"))
                .thenReturn(1);

        AsistenciaHoyRespuesta respuesta =
                gestor(reloj(17, 0)).registrarSalida(personal, "  Cita médica  ");

        assertEquals(LocalTime.of(17, 0), respuesta.horaSalida());
        assertTrue(respuesta.salidaAnticipada());
        assertEquals(0, respuesta.minutosTarde());
        verify(repositorio).registrarSalida(20L, LocalTime.of(17, 0), true, "Cita médica");
    }

    @Test
    void registraLaSalidaNormalDesdeElCierreSinMotivo() {
        when(repositorio.findByPersonalIdAndFecha(5L, HOY)).thenReturn(
                Optional.of(conEntrada()),
                Optional.of(conSalida(LocalTime.of(18, 0), false, null)));
        when(repositorio.registrarSalida(20L, LocalTime.of(18, 0), false, null))
                .thenReturn(1);

        AsistenciaHoyRespuesta respuesta =
                gestor(reloj(18, 0)).registrarSalida(personal, "motivo que se ignora");

        assertEquals(LocalTime.of(18, 0), respuesta.horaSalida());
        assertFalse(respuesta.salidaAnticipada());
        verify(repositorio).registrarSalida(20L, LocalTime.of(18, 0), false, null);
    }

    @Test
    void noPermiteUnaSegundaSalida() {
        when(repositorio.findByPersonalIdAndFecha(5L, HOY))
                .thenReturn(Optional.of(conSalida(LocalTime.of(17, 0), false, null)));

        ExcepcionReglaNegocio excepcion = assertThrows(ExcepcionReglaNegocio.class,
                () -> gestor(reloj(18, 30)).registrarSalida(personal, null));

        assertEquals("SALIDA_YA_REGISTRADA", excepcion.getCodigo());
    }

    @Test
    void laActualizacionCondicionadaDetectaLaSegundaSalida() {
        when(repositorio.findByPersonalIdAndFecha(5L, HOY))
                .thenReturn(Optional.of(conEntrada()));
        when(repositorio.registrarSalida(20L, LocalTime.of(18, 0), false, null))
                .thenReturn(0);

        ExcepcionReglaNegocio excepcion = assertThrows(ExcepcionReglaNegocio.class,
                () -> gestor(reloj(18, 0)).registrarSalida(personal, null));

        assertEquals("SALIDA_YA_REGISTRADA", excepcion.getCodigo());
    }

    @Test
    void laSalidaNoEstaHabilitadaAntesDelCierre() {
        when(repositorio.findByPersonalIdAndFecha(5L, HOY))
                .thenReturn(Optional.of(conEntrada()));

        AsistenciaHoyRespuesta respuesta = gestor(reloj(17, 59)).obtenerHoy(personal);

        assertFalse(respuesta.puedeRegistrarSalida());
        assertEquals(LocalTime.of(18, 0), respuesta.horaCierre());
        assertNull(respuesta.horaSalida());
    }

    @Test
    void laSalidaSeHabilitaEnLaHoraDeCierre() {
        when(repositorio.findByPersonalIdAndFecha(5L, HOY))
                .thenReturn(Optional.of(conEntrada()));

        AsistenciaHoyRespuesta respuesta = gestor(reloj(18, 0)).obtenerHoy(personal);

        assertTrue(respuesta.puedeRegistrarSalida());
    }

    @Test
    void despuesDelCierreSinEntradaNoSePuedeRegistrarNada() {
        when(repositorio.findByPersonalIdAndFecha(5L, HOY)).thenReturn(Optional.empty());

        AsistenciaHoyRespuesta respuesta = gestor(reloj(18, 30)).obtenerHoy(personal);

        assertFalse(respuesta.puedeRegistrarEntrada());
        assertFalse(respuesta.puedeRegistrarSalida());
    }

    @Test
    void listaTodosLosEstilistasYMarcaLosQueNoRegistraronEntrada() {
        Estilista luis = new Estilista();
        luis.setId(6L);
        luis.setNombre("Luis");
        luis.setEmail("luis@correo.com");

        Asistencia deMarta = new Asistencia();
        deMarta.setId(20L);
        deMarta.setPersonal(personal);
        deMarta.setFecha(HOY);
        deMarta.setHoraEntrada(LocalTime.of(8, 12));
        deMarta.setHoraSalida(LocalTime.of(17, 0));
        deMarta.setMinutosTarde(12);
        deMarta.setSalidaAnticipada(true);
        deMarta.setMotivoSalida("Cita médica");

        when(repositorioEstilista.findByActivoTrueOrderByNombreAsc())
                .thenReturn(List.of(luis, personal));
        when(repositorio.findByFecha(HOY)).thenReturn(List.of(deMarta));

        List<AsistenciaPersonalRespuesta> filas = gestor(reloj(18, 0)).listarPorFecha(HOY);

        assertEquals(2, filas.size());
        assertEquals("Luis", filas.get(0).nombre());
        assertFalse(filas.get(0).trabajo());
        assertNull(filas.get(0).horaEntrada());

        assertEquals("Marta", filas.get(1).nombre());
        assertTrue(filas.get(1).trabajo());
        assertTrue(filas.get(1).llegoTarde());
        assertEquals(12, filas.get(1).minutosTarde());
        assertTrue(filas.get(1).salidaAnticipada());
        assertEquals("Cita médica", filas.get(1).motivoSalida());
    }

    @Test
    void sinFechaListaElDiaDelRelojDelServidor() {
        when(repositorioEstilista.findByActivoTrueOrderByNombreAsc()).thenReturn(List.of(personal));
        when(repositorio.findByFecha(HOY)).thenReturn(List.of());

        gestor(reloj(10, 0)).listarPorFecha(null);

        verify(repositorio).findByFecha(HOY);
    }
}
