package com.ucab.peluqueria.modelo;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "servicios")
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    /** Costo informativo: el sistema no cobra en línea. Debe ser mayor que 0. */
    @NotNull
    @DecimalMin(value = "0.00", inclusive = false)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal costo;

    /** Tiempo de ejecución en minutos. Define cuánto ocupa la cita en la agenda. */
    @NotNull
    @Positive
    @Column(name = "duracion_minutos", nullable = false)
    private Integer duracionMinutos;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "tiene_imagen", nullable = false, columnDefinition = "boolean default false")
    private boolean tieneImagen = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getCosto() { return costo; }
    public void setCosto(BigDecimal costo) { this.costo = costo; }

    public Integer getDuracionMinutos() { return duracionMinutos; }
    public void setDuracionMinutos(Integer duracionMinutos) { this.duracionMinutos = duracionMinutos; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public boolean isTieneImagen() { return tieneImagen; }
    public void setTieneImagen(boolean tieneImagen) { this.tieneImagen = tieneImagen; }

    // Servicio se guarda en un Set (Estilista.servicios): igualdad por id, sin romper los proxies de Hibernate.
    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof Servicio servicio)) {
            return false;
        }
        return getId() != null && getId().equals(servicio.getId());
    }

    @Override
    public int hashCode() {
        return Servicio.class.hashCode();
    }
}