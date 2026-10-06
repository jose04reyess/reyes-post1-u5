package com.universidad.reservaslabs.exception;

/**
 * Excepción lanzada cuando una operación de reserva genera un conflicto de negocio,
 * como solapamientos de horarios, franjas fuera del horario de atención, duración inválida
 * o intentos de cancelar reservas pasadas.
 */
public class ReservaConflictException extends RuntimeException {

    public ReservaConflictException(String message) {
        super(message);
    }

    public ReservaConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
