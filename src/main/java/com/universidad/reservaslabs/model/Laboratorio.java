package com.universidad.reservaslabs.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa un laboratorio disponible en la institución universitaria.
 */
@Entity
@Table(name = "laboratorios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Laboratorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del laboratorio es obligatorio")
    @Column(nullable = false, unique = true)
    private String nombre;

    @NotBlank(message = "La ubicación del laboratorio es obligatoria")
    @Column(nullable = false)
    private String ubicacion;

    @Min(value = 1, message = "La capacidad debe ser de al menos 1 persona")
    @Column(nullable = false)
    private Integer capacidad;

    @NotBlank(message = "El tipo de laboratorio es obligatorio")
    @Column(nullable = false)
    private String tipo;

    public Laboratorio(String nombre, String ubicacion, Integer capacidad, String tipo) {
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.capacidad = capacidad;
        this.tipo = tipo;
    }
}
