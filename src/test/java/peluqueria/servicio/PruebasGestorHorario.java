package peluqueria.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import peluqueria.configuracion.PropiedadesHorario;
import peluqueria.excepcion.ExcepcionReglaNegocio;
import peluqueria.modelo.HorarioLocal;
import peluqueria.repositorio.RepositorioHorario;

/** Pruebas del horario editable del local. */
class PruebasGestorHorario {

    private static final ZoneId ZONA = ZoneId.of("America/Caracas");

    private RepositorioHorario repositorio;
    private GestorHorario gestor;

    @BeforeEach
    void preparar() {
        repositorio = mock(RepositorioHorario.class);
        when(repositorio.save(any(HorarioLocal.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));
        gestor = new GestorHorario(repositorio, new PropiedadesHorario(
                ZONA, LocalTime.of(8, 0), LocalTime.of(18, 0), 0));
    }

    @Test
    void creaElHorarioInicialDesdeLaConfiguracion() {
        when(repositorio.findById(1L)).thenReturn(Optional.empty());

        PropiedadesHorario actual = gestor.actual();

        assertEquals(LocalTime.of(8, 0), actual.apertura());
        assertEquals(LocalTime.of(18, 0), actual.cierre());
        verify(repositorio).save(any(HorarioLocal.class));
    }

    @Test
    void devuelveElHorarioGuardado() {
        HorarioLocal guardado = new HorarioLocal();
        guardado.setId(1L);
        guardado.setApertura(LocalTime.of(9, 0));
        guardado.setCierre(LocalTime.of(20, 0));
        when(repositorio.findById(1L)).thenReturn(Optional.of(guardado));

        PropiedadesHorario actual = gestor.actual();

        assertEquals(LocalTime.of(9, 0), actual.apertura());
        assertEquals(LocalTime.of(20, 0), actual.cierre());
    }

    @Test
    void actualizaAperturaYCierre() {
        HorarioLocal guardado = new HorarioLocal();
        guardado.setId(1L);
        guardado.setApertura(LocalTime.of(8, 0));
        guardado.setCierre(LocalTime.of(18, 0));
        when(repositorio.findById(1L)).thenReturn(Optional.of(guardado));

        PropiedadesHorario actual = gestor.actualizar(
                LocalTime.of(10, 0), LocalTime.of(19, 0), "admin@correo.com");

        assertEquals(LocalTime.of(10, 0), actual.apertura());
        assertEquals(LocalTime.of(19, 0), actual.cierre());
        assertEquals(LocalTime.of(10, 0), guardado.getApertura());
        verify(repositorio).save(guardado);
    }

    @Test
    void rechazaUnaAperturaQueNoEsAnteriorAlCierre() {
        ExcepcionReglaNegocio excepcion = assertThrows(ExcepcionReglaNegocio.class,
                () -> gestor.actualizar(
                        LocalTime.of(19, 0), LocalTime.of(18, 0), "admin@correo.com"));

        assertEquals("HORARIO_INVALIDO", excepcion.getCodigo());
        assertEquals(HttpStatus.BAD_REQUEST, excepcion.getStatus());
        verify(repositorio, never()).save(any());
    }
}
