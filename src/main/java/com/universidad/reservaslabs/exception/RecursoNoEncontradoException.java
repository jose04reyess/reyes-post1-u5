package com.universidad.reservaslabs.exception;

/**
 * Excepción lanzada cuando un recurso solicitado (Laboratorio o Reserva) no existe en el sistema.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String message) {
        super(message);
    }

    public RecursoNoEncontradoException(String message, Throwable cause) {
        super(message, cause);
    }
}
