package com.ucab.peluqueria.configuracion;

import java.time.Clock;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra el horario del local y el reloj del servidor con la zona configurada.
 * La asistencia toma de aquí la hora, nunca del navegador.
 */
@Configuration
@EnableConfigurationProperties(PropiedadesHorario.class)
public class ConfiguracionHorario {

    /** Reloj del servidor en la zona del local; las pruebas lo sustituyen por uno fijo. */
    @Bean
    public Clock reloj(PropiedadesHorario propiedades) {
        return Clock.system(propiedades.zona());
    }
}
