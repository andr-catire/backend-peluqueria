package peluqueria.configuracion;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/** Comprueba el enlace de app.horario.* y que la aplicación no arranca con un horario inválido. */
class PruebasConfiguracionHorario {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(ConfiguracionHorario.class)
            .withPropertyValues(
                    "app.horario.zona=America/Caracas",
                    "app.horario.apertura=08:00",
                    "app.horario.cierre=18:00",
                    "app.horario.tolerancia-minutos=0");

    @Test
    void creaElRelojEnLaZonaConfigurada() {
        runner.run(contexto -> {
            assertThat(contexto).hasNotFailed();
            assertThat(contexto.getBean(Clock.class).getZone())
                    .isEqualTo(ZoneId.of("America/Caracas"));
        });
    }

    @Test
    void noArrancaSiLaAperturaNoEsAnteriorAlCierre() {
        runner.withPropertyValues("app.horario.apertura=19:00")
                .run(contexto -> assertThat(contexto).hasFailed());
    }
}
