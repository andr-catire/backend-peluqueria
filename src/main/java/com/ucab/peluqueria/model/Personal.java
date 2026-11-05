package com.ucab.peluqueria.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

/**
 * Estilista de la peluquería (el "Personal" del Brief). Que tenga al menos un servicio asociado
 * se valida en el DTO de registro, no aquí: la colección es LAZY y validarla en la entidad
 * obligaría a cargarla en cada actualización.
 */
@Entity
@Table(name = "personal")
public class Personal extends Usuario {

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "personal_servicios",
            joinColumns = @JoinColumn(name = "personal_id"),
            inverseJoinColumns = @JoinColumn(name = "servicio_id"))
    private Set<Servicio> servicios = new HashSet<>();

    public Personal() {
        setRol(Rol.PERSONAL);
    }

    public Set<Servicio> getServicios() { return servicios; }
    public void setServicios(Set<Servicio> servicios) { this.servicios = servicios; }
}