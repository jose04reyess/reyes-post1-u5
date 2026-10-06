package com.universidad.reservaslabs.service;

import com.universidad.reservaslabs.exception.RecursoNoEncontradoException;
import com.universidad.reservaslabs.exception.ReservaConflictException;
import com.universidad.reservaslabs.model.EstadoReserva;
import com.universidad.reservaslabs.model.Laboratorio;
import com.universidad.reservaslabs.model.Reserva;
import com.universidad.reservaslabs.repository.LaboratorioRepository;
import com.universidad.reservaslabs.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Capa de Servicio que centraliza todas las reglas de negocio y transacciones para la gestión de reservas.
 */
@Service
@Transactional
public class ReservaService {

    public static final LocalTime APERTURA = LocalTime.of(7, 0);
    public static final LocalTime CIERRE = LocalTime.of(21, 0);
    public static final Duration DURACION_MINIMA = Duration.ofMinutes(30);
    public static final Duration DURACION_MAXIMA = Duration.ofHours(3);

    private final ReservaRepository reservaRepository;
    private final LaboratorioRepository laboratorioRepository;

    public ReservaService(ReservaRepository reservaRepository, LaboratorioRepository laboratorioRepository) {
        this.reservaRepository = reservaRepository;
        this.laboratorioRepository = laboratorioRepository;
    }

    /**
     * Valida reglas de horario institucional y límites de duración en memoria (sin consultas a base de datos).
     */
    public void validarHorarioYDuracion(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null) {
            throw new ReservaConflictException("Las fechas y horas de inicio y fin son obligatorias");
        }

        if (!fin.isAfter(inicio)) {
            throw new ReservaConflictException("La fecha/hora de fin debe ser estrictamente posterior a la fecha/hora de inicio");
        }

        if (!inicio.toLocalDate().isEqual(fin.toLocalDate())) {
            throw new ReservaConflictException("La reserva debe comenzar y finalizar en el mismo día");
        }

        LocalTime horaInicio = inicio.toLocalTime();
        LocalTime horaFin = fin.toLocalTime();

        if (horaInicio.isBefore(APERTURA) || horaFin.isAfter(CIERRE)) {
            throw new ReservaConflictException("El horario permitido de reserva es entre las 07:00 y las 21:00");
        }

        Duration duracion = Duration.between(inicio, fin);
        if (duracion.compareTo(DURACION_MINIMA) < 0 || duracion.compareTo(DURACION_MAXIMA) > 0) {
            throw new ReservaConflictException("La duración de la reserva debe ser de mínimo 30 minutos y máximo 3 horas");
        }
    }

    /**
     * Crea una nueva reserva verificando la existencia del laboratorio, validando las franjas horarias
     * y comprobando ausencia de solapamientos mediante consulta optimizada en base de datos.
     */
    public Reserva crear(Reserva reserva) {
        if (reserva.getLaboratorio() == null || reserva.getLaboratorio().getId() == null) {
            throw new RecursoNoEncontradoException("Debe especificarse un laboratorio válido para la reserva");
        }

        Long laboratorioId = reserva.getLaboratorio().getId();
        Laboratorio laboratorio = laboratorioRepository.findById(laboratorioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Laboratorio no encontrado con ID: " + laboratorioId));

        reserva.setLaboratorio(laboratorio);

        // Validación pura en memoria de formato, franja y duración
        validarHorarioYDuracion(reserva.getInicio(), reserva.getFin());

        // Verificación de solapamientos optimizada en base de datos
        List<Reserva> solapamientos = reservaRepository.buscarSolapamientos(
                laboratorio.getId(),
                reserva.getInicio(),
                reserva.getFin()
        );

        if (!solapamientos.isEmpty()) {
            throw new ReservaConflictException("El laboratorio " + laboratorio.getNombre() + " ya tiene una reserva en ese horario");
        }

        reserva.setEstado(EstadoReserva.CONFIRMADA);
        return reservaRepository.save(reserva);
    }

    /**
     * Cancela una reserva verificando que no haya iniciado previamente.
     */
    public Reserva cancelar(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva no encontrada con ID: " + id));

        if (reserva.getInicio().isBefore(LocalDateTime.now())) {
            throw new ReservaConflictException("No se puede cancelar una reserva cuyo horario de inicio ya pasó");
        }

        reserva.setEstado(EstadoReserva.CANCELADA);
        return reservaRepository.save(reserva);
    }

    @Transactional(readOnly = true)
    public List<Reserva> findAll() {
        return reservaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Reserva findById(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva no encontrada con ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Reserva> findByLaboratorio(Long laboratorioId) {
        if (!laboratorioRepository.existsById(laboratorioId)) {
            throw new RecursoNoEncontradoException("Laboratorio no encontrado con ID: " + laboratorioId);
        }
        return reservaRepository.findByLaboratorioId(laboratorioId);
    }
}
