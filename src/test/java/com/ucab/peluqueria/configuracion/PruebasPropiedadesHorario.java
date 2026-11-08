package com.ucab.peluqueria.configuracion;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalTime;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

/** Comprueba que un horario incoherente impide arrancar la aplicación. */
class PruebasPropiedadesHorario {

    private static final ZoneId ZONA = ZoneId.of("America/Caracas");

    @Test
    void aceptaUnaAperturaAnteriorAlCierre() {
        assertDoesNotThrow(() -> new PropiedadesHorario(
                ZONA, LocalTime.of(8, 0), LocalTime.of(18, 0), 0));
    }

    @Test
    void rechazaUnaAperturaIgualAlCierre() {
        assertThrows(IllegalStateException.class, () -> new PropiedadesHorario(
                ZONA, LocalTime.of(18, 0), LocalTime.of(18, 0), 0));
    }

    @Test
    void rechazaUnaAperturaPosteriorAlCierre() {
        assertThrows(IllegalStateException.class, () -> new PropiedadesHorario(
                ZONA, LocalTime.of(19, 0), LocalTime.of(18, 0), 0));
    }

    @Test
    void rechazaUnaToleranciaNegativa() {
        assertThrows(IllegalStateException.class, () -> new PropiedadesHorario(
                ZONA, LocalTime.of(8, 0), LocalTime.of(18, 0), -1));
    }
}
