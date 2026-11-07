package com.ucab.peluqueria.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Contenido binario de la imagen de un servicio. La usa GestorImagenes; su id es el del servicio.
 */
@Entity
@Table(name = "servicio_imagenes")
public class ImagenServicio {

    @Id
    private Long id;

    @Column(nullable = false, length = 30)
    private String tipo;

    @Column(nullable = false, columnDefinition = "bytea")
    private byte[] datos;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public byte[] getDatos() { return datos; }
    public void setDatos(byte[] datos) { this.datos = datos; }
}
