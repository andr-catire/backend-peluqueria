package peluqueria.modelo;

import java.math.BigDecimal;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * Reserva de un cliente dentro de la agenda de un estilista. La fecha y el estilista salen de la
 * agenda (cita.getAgenda().getFecha() y .getEstilista()); aquí solo se guarda el tramo horario.
 * La base de datos impide que dos citas no canceladas de la misma agenda se solapen.
 */
@Entity
@Table(name = "citas")
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agenda_id", nullable = false)
    private Agenda agenda;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    @NotNull
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    /** horaInicio más la duración del servicio; la calcula el servicio al agendar. */
    @NotNull
    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    /** Costo del servicio al momento de reservar: así un cambio de precio posterior no altera el pasado. */
    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal costo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCita estado = EstadoCita.PENDIENTE;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Agenda getAgenda() { return agenda; }
    public void setAgenda(Agenda agenda) { this.agenda = agenda; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Servicio getServicio() { return servicio; }
    public void setServicio(Servicio servicio) { this.servicio = servicio; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public BigDecimal getCosto() { return costo; }
    public void setCosto(BigDecimal costo) { this.costo = costo; }

    public EstadoCita getEstado() { return estado; }
    public void setEstado(EstadoCita estado) { this.estado = estado; }
}