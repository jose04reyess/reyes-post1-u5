package com.universidad.reservaslabs;

import com.universidad.reservaslabs.model.EstadoReserva;
import com.universidad.reservaslabs.model.Laboratorio;
import com.universidad.reservaslabs.model.Reserva;
import com.universidad.reservaslabs.repository.LaboratorioRepository;
import com.universidad.reservaslabs.repository.ReservaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Componente que inicializa la base de datos H2 en memoria con laboratorios de catálogo
 * y una reserva inicial de prueba para facilitar la demostración inmediata y pruebas de solapamiento.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final LaboratorioRepository laboratorioRepository;
    private final ReservaRepository reservaRepository;

    public DataInitializer(LaboratorioRepository laboratorioRepository, ReservaRepository reservaRepository) {
        this.laboratorioRepository = laboratorioRepository;
        this.reservaRepository = reservaRepository;
    }

    @Override
    public void run(String... args) {
        if (laboratorioRepository.count() == 0) {
            Laboratorio lab1 = laboratorioRepository.save(new Laboratorio(
                    "Laboratorio de Cómputo A",
                    "Edificio 1 - Piso 2",
                    30,
                    "Informática"
            ));

            Laboratorio lab2 = laboratorioRepository.save(new Laboratorio(
                    "Laboratorio de Redes y Telecomunicaciones",
                    "Edificio 3 - Piso 1",
                    25,
                    "Telecomunicaciones"
            ));

            Laboratorio lab3 = laboratorioRepository.save(new Laboratorio(
                    "Laboratorio de Física y Electrónica",
                    "Edificio 2 - Piso 1",
                    20,
                    "Ciencias Básicas"
            ));

            // Reserva inicial de prueba: Mañana de 08:00 a 10:00 en Laboratorio de Cómputo A
            LocalDateTime inicioPrueba = LocalDate.now().plusDays(1).atTime(8, 0);
            LocalDateTime finPrueba = LocalDate.now().plusDays(1).atTime(10, 0);

            Reserva reservaInicial = new Reserva(
                    lab1,
                    "Prof. Carlos Rodríguez",
                    "carlos.rodriguez@universidad.edu",
                    inicioPrueba,
                    finPrueba,
                    "Clase magistral de Programación Avanzada",
                    EstadoReserva.CONFIRMADA
            );
            reservaRepository.save(reservaInicial);
        }
    }
}
