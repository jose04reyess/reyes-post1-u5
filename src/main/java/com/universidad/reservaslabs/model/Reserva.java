package com.universidad.reservaslabs.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa una reserva de un laboratorio para una franja horaria determinada.
 */
@Entity
@Table(name = "reservas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "laboratorio_id", nullable = false)
    @NotNull(message = "El laboratorio a reservar es obligatorio")
    private Laboratorio laboratorio;

    @NotBlank(message = "El nombre del solicitante es obligatorio")
    @Column(name = "nombre_solicitante", nullable = false)
    private String nombreSolicitante;

    @NotBlank(message = "El correo del solicitante es obligatorio")
    @Email(message = "El correo electrónico debe ser válido")
    @Column(name = "correo_solicitante", nullable = false)
    private String correoSolicitante;

    @NotNull(message = "La fecha y hora de inicio es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(name = "inicio", nullable = false)
    private LocalDateTime inicio;

    @NotNull(message = "La fecha y hora de finalización es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(name = "fin", nullable = false)
    private LocalDateTime fin;

    @Column(name = "motivo")
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoReserva estado = EstadoReserva.CONFIRMADA;

    public Reserva(Laboratorio laboratorio, String nombreSolicitante, String correoSolicitante,
                   LocalDateTime inicio, LocalDateTime fin, String motivo) {
        this.laboratorio = laboratorio;
        this.nombreSolicitante = nombreSolicitante;
        this.correoSolicitante = correoSolicitante;
        this.inicio = inicio;
        this.fin = fin;
        this.motivo = motivo;
        this.estado = EstadoReserva.CONFIRMADA;
    }

    public Reserva(Laboratorio laboratorio, String nombreSolicitante, String correoSolicitante,
                   LocalDateTime inicio, LocalDateTime fin, String motivo, EstadoReserva estado) {
        this.laboratorio = laboratorio;
        this.nombreSolicitante = nombreSolicitante;
        this.correoSolicitante = correoSolicitante;
        this.inicio = inicio;
        this.fin = fin;
        this.motivo = motivo;
        this.estado = estado;
    }
}
