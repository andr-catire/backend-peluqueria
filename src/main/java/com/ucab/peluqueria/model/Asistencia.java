package com.ucab.peluqueria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "asistencias")
public class Asistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cita_id", nullable = false, unique = true)
    private Cita cita;

    @Column(name = "cliente_confirmo", nullable = false)
    private boolean clienteConfirmo;

    @Column(name = "estilista_confirmo", nullable = false)
    private boolean estilistaConfirmo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cita getCita() { return cita; }
    public void setCita(Cita cita) { this.cita = cita; }

    public boolean isClienteConfirmo() { return clienteConfirmo; }
    public void setClienteConfirmo(boolean clienteConfirmo) { this.clienteConfirmo = clienteConfirmo; }

    public boolean isEstilistaConfirmo() { return estilistaConfirmo; }
    public void setEstilistaConfirmo(boolean estilistaConfirmo) { this.estilistaConfirmo = estilistaConfirmo; }
}
