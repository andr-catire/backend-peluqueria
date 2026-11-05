package com.ucab.peluqueria.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;

/**
 * Agenda diaria de un estilista: contiene las citas de ese estilista en una fecha.
 * Hay una sola agenda por estilista y día; se crea al reservar la primera cita de esa fecha.
 */
@Entity
@Table(
        name = "agendas",
        uniqueConstraints = @UniqueConstraint(
                name = "ux_agendas_estilista_fecha", columnNames = {"estilista_id", "fecha"}))
public class Agenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estilista_id", nullable = false)
    private Personal estilista;

    @NotNull
    @Column(nullable = false)
    private LocalDate fecha;

    /**
     * Citas de esta agenda. Es solo de lectura: las citas se guardan con CitaRepository, y la
     * disponibilidad se consulta con una consulta, no recorriendo esta lista.
     */
    @OneToMany(mappedBy = "agenda", fetch = FetchType.LAZY)
    private List<Cita> citas = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Personal getEstilista() { return estilista; }
    public void setEstilista(Personal estilista) { this.estilista = estilista; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public List<Cita> getCitas() { return citas; }
}