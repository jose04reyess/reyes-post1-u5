package com.universidad.reservaslabs.repository;

import com.universidad.reservaslabs.model.Laboratorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad Laboratorio.
 */
@Repository
public interface LaboratorioRepository extends JpaRepository<Laboratorio, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    Optional<Laboratorio> findByNombreIgnoreCase(String nombre);
}
