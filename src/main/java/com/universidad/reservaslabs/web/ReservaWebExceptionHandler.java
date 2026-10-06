package com.universidad.reservaslabs.web;

import com.universidad.reservaslabs.exception.RecursoNoEncontradoException;
import com.universidad.reservaslabs.exception.ReservaConflictException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Manejador de excepciones específico para la interfaz Web MVC (Thymeleaf).
 * Convierte las excepciones de negocio del dominio en redirecciones con mensajes flash para el usuario final.
 */
@ControllerAdvice(assignableTypes = ReservaWebController.class)
public class ReservaWebExceptionHandler {

    @ExceptionHandler(ReservaConflictException.class)
    public String manejarConflictoReserva(ReservaConflictException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/reservas/nueva";
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public String manejarRecursoNoEncontrado(RecursoNoEncontradoException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/reservas";
    }
}
