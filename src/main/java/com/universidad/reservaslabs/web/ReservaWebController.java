package com.universidad.reservaslabs.web;

import com.universidad.reservaslabs.model.Reserva;
import com.universidad.reservaslabs.repository.LaboratorioRepository;
import com.universidad.reservaslabs.service.ReservaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador Web MVC con Thymeleaf para la interfaz de usuario de Reservas.
 * Comparte exactamente el mismo bean singleton de ReservaService y LaboratorioRepository que la API REST.
 */
@Controller
@RequestMapping("/reservas")
public class ReservaWebController {

    private final ReservaService reservaService;
    private final LaboratorioRepository laboratorioRepository;

    public ReservaWebController(ReservaService reservaService, LaboratorioRepository laboratorioRepository) {
        this.reservaService = reservaService;
        this.laboratorioRepository = laboratorioRepository;
    }

    @GetMapping
    public String listarReservas(Model model) {
        model.addAttribute("reservas", reservaService.findAll());
        model.addAttribute("laboratorios", laboratorioRepository.findAll());
        return "reservas/lista";
    }

    @GetMapping("/nueva")
    public String formularioNuevaReserva(Model model) {
        if (!model.containsAttribute("reserva")) {
            model.addAttribute("reserva", new Reserva());
        }
        model.addAttribute("laboratorios", laboratorioRepository.findAll());
        return "reservas/nueva";
    }

    @PostMapping
    public String guardarReserva(@ModelAttribute Reserva reserva, RedirectAttributes redirectAttributes) {
        reservaService.crear(reserva);
        redirectAttributes.addFlashAttribute("mensaje", "¡Reserva creada exitosamente con estado CONFIRMADA!");
        return "redirect:/reservas";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelarReserva(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        reservaService.cancelar(id);
        redirectAttributes.addFlashAttribute("mensaje", "Reserva #" + id + " cancelada correctamente.");
        return "redirect:/reservas";
    }
}
