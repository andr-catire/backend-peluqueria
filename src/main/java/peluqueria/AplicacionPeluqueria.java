package peluqueria;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class AplicacionPeluqueria {

	public static void main(String[] args) {
		SpringApplication.run(AplicacionPeluqueria.class, args);
	}

	/**
	 * Fija la zona horaria del JVM para toda la aplicación.
	 * Sin esto, la zona depende de la máquina donde corra el backend y las fechas se desfasan
	 * respecto a la hora del salón.
	 */
	@PostConstruct
	public void configurarZonaHoraria() {
		TimeZone.setDefault(TimeZone.getTimeZone("America/Caracas"));
	}

}
