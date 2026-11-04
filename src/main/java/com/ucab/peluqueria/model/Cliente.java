 
package com.ucab.peluqueria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "clientes")
public class Cliente extends Usuario {

    @NotBlank
    @Column(nullable = false, unique = true, length = 20)
    private String cedula;

    @NotBlank
    @Column(nullable = false, length = 30)
    private String telefono;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estilista_habitual_id")
    private Personal estilistaHabitual;

    public Cliente() {
        setRol(Rol.CLIENTE);
    }

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public Personal getEstilistaHabitual() { return estilistaHabitual; }
    public void setEstilistaHabitual(Personal estilistaHabitual) { this.estilistaHabitual = estilistaHabitual; }
}