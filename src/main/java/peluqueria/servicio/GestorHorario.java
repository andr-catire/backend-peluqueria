package peluqueria.servicio;

import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import peluqueria.configuracion.PropiedadesHorario;
import peluqueria.excepcion.ExcepcionReglaNegocio;
import peluqueria.modelo.HorarioLocal;
import peluqueria.repositorio.RepositorioHorario;

/**
 * Mantiene el horario vigente del local: lo inicializa desde la configuración y permite
 * que el administrador cambie apertura y cierre sin reiniciar la aplicación.
 */
@Service
@Order(1)
public class GestorHorario implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(GestorHorario.class);
    private static final Long ID_UNICO = 1L;

    private final RepositorioHorario repositorioHorario;
    private final PropiedadesHorario propiedades;

    public GestorHorario(RepositorioHorario repositorioHorario, PropiedadesHorario propiedades) {
        this.repositorioHorario = repositorioHorario;
        this.propiedades = propiedades;
    }

    @Override
    @Transactional
    public void run(String... args) {
        asegurarHorario();
    }

    /** Horario vigente, combinado con la zona y la tolerancia de la configuración. */
    @Transactional
    public PropiedadesHorario actual() {
        return combinar(asegurarHorario());
    }

    /** Cambia apertura y cierre; la apertura debe ser anterior al cierre. */
    @Transactional
    public PropiedadesHorario actualizar(LocalTime apertura, LocalTime cierre, String adminEmail) {
        if (!apertura.isBefore(cierre)) {
            throw ExcepcionReglaNegocio.invalida(
                    "HORARIO_INVALIDO",
                    "La hora de apertura debe ser anterior a la hora de cierre.");
        }

        HorarioLocal horario = asegurarHorario();
        horario.setApertura(apertura);
        horario.setCierre(cierre);
        repositorioHorario.save(horario);

        log.info("AUDITORIA accion=HORARIO_ACTUALIZADO admin={} apertura={} cierre={}",
                adminEmail, apertura, cierre);

        return combinar(horario);
    }

    private HorarioLocal asegurarHorario() {
        return repositorioHorario.findById(ID_UNICO).orElseGet(() -> {
            HorarioLocal nuevo = new HorarioLocal();
            nuevo.setId(ID_UNICO);
            nuevo.setApertura(propiedades.apertura());
            nuevo.setCierre(propiedades.cierre());
            return repositorioHorario.save(nuevo);
        });
    }

    private PropiedadesHorario combinar(HorarioLocal horario) {
        return new PropiedadesHorario(
                propiedades.zona(),
                horario.getApertura(),
                horario.getCierre(),
                propiedades.toleranciaMinutos());
    }
}
