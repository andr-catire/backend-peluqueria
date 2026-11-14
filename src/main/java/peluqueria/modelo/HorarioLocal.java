package peluqueria.modelo;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/** Horario de apertura y cierre del local. Se guarda en una sola fila con id 1. */
@Entity
@Table(name = "horario_local")
public class HorarioLocal {

    @Id
    private Long id = 1L;

    @NotNull
    @Column(nullable = false)
    private LocalTime apertura;

    @NotNull
    @Column(nullable = false)
    private LocalTime cierre;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalTime getApertura() { return apertura; }
    public void setApertura(LocalTime apertura) { this.apertura = apertura; }

    public LocalTime getCierre() { return cierre; }
    public void setCierre(LocalTime cierre) { this.cierre = cierre; }
}
