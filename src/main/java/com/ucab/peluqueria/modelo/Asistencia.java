package com.ucab.peluqueria.modelo;

import java.time.LocalDate;
import java.time.LocalTime;

import org.hibernate.annotations.Check;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;

/** Registro de entrada y salida de un estilista en un día (una fila por estilista y fecha). */
@Entity
@Table(
        name = "asistencias",
        uniqueConstraints = @UniqueConstraint(
                name = "ux_asistencias_personal_fecha", columnNames = {"personal_id", "fecha"}))
@Check(name = "ck_asistencias_minutos", constraints = "minutos_tarde >= 0")
public class Asistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personal_id", nullable = false)
    private Estilista personal;

    @NotNull
    @Column(nullable = false)
    private LocalDate fecha;

    @NotNull
    @Column(name = "hora_entrada", nullable = false)
    private LocalTime horaEntrada;

    /** Queda vacía hasta que se registra la salida. */
    @Column(name = "hora_salida")
    private LocalTime horaSalida;

    /** Minutos desde la apertura cuando la entrada supera la tolerancia; 0 si fue puntual. */
    @Column(name = "minutos_tarde", nullable = false, columnDefinition = "integer default 0")
    private int minutosTarde;

    @Column(name = "salida_anticipada", nullable = false, columnDefinition = "boolean default false")
    private boolean salidaAnticipada;

    @Column(name = "motivo_salida", length = 200)
    private String motivoSalida;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Estilista getPersonal() { return personal; }
    public void setPersonal(Estilista personal) { this.personal = personal; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHoraEntrada() { return horaEntrada; }
    public void setHoraEntrada(LocalTime horaEntrada) { this.horaEntrada = horaEntrada; }

    public LocalTime getHoraSalida() { return horaSalida; }
    public void setHoraSalida(LocalTime horaSalida) { this.horaSalida = horaSalida; }

    public int getMinutosTarde() { return minutosTarde; }
    public void setMinutosTarde(int minutosTarde) { this.minutosTarde = minutosTarde; }

    public boolean isSalidaAnticipada() { return salidaAnticipada; }
    public void setSalidaAnticipada(boolean salidaAnticipada) { this.salidaAnticipada = salidaAnticipada; }

    public String getMotivoSalida() { return motivoSalida; }
    public void setMotivoSalida(String motivoSalida) { this.motivoSalida = motivoSalida; }

    /** Derivado: se llegó tarde cuando hay al menos un minuto de retraso. */
    public boolean isLlegoTarde() { return minutosTarde > 0; }
}