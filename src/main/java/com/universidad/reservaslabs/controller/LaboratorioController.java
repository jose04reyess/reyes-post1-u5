package com.universidad.reservaslabs.controller;

import com.universidad.reservaslabs.exception.RecursoNoEncontradoException;
import com.universidad.reservaslabs.model.Laboratorio;
import com.universidad.reservaslabs.repository.LaboratorioRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para la gestión directa del catálogo de Laboratorios.
 * Inyecta directamente LaboratorioRepository al ser operaciones CRUD directas sin reglas complejas de negocio,
 * evitando la creación de un servicio anémico redundante.
 */
@RestController
@RequestMapping("/api/laboratorios")
public class LaboratorioController {

    private final LaboratorioRepository laboratorioRepository;

    public LaboratorioController(LaboratorioRepository laboratorioRepository) {
        this.laboratorioRepository = laboratorioRepository;
    }

    @GetMapping
    public List<Laboratorio> listarTodos() {
        return laboratorioRepository.findAll();
    }

    @GetMapping("/{id}")
    public Laboratorio obtenerPorId(@PathVariable Long id) {
        return laboratorioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Laboratorio no encontrado con ID: " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Laboratorio crear(@Valid @RequestBody Laboratorio laboratorio) {
        return laboratorioRepository.save(laboratorio);
    }
}
