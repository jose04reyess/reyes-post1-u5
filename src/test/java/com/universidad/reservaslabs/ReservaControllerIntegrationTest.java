package com.universidad.reservaslabs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.universidad.reservaslabs.model.Laboratorio;
import com.universidad.reservaslabs.model.Reserva;
import com.universidad.reservaslabs.repository.LaboratorioRepository;
import com.universidad.reservaslabs.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReservaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LaboratorioRepository laboratorioRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    private Laboratorio laboratorio;

    @BeforeEach
    void setUp() {
        reservaRepository.deleteAll();
        laboratorioRepository.deleteAll();

        laboratorio = laboratorioRepository.save(new Laboratorio(
                "Laboratorio de Cómputo A",
                "Edificio 1 - Piso 2",
                30,
                "Informática"
        ));
    }

    @Test
    @DisplayName("POST /api/reservas -> 201 Created cuando no hay conflicto de horario")
    void testCrearReservaExitosaHttp201() throws Exception {
        LocalDate fecha = LocalDate.now().plusDays(3);
        LocalDateTime inicio = fecha.atTime(10, 0);
        LocalDateTime fin = fecha.atTime(12, 0);

        Reserva reserva = new Reserva();
        reserva.setLaboratorio(laboratorio);
        reserva.setNombreSolicitante("Ing. Juan Valdés");
        reserva.setCorreoSolicitante("juan.valdes@universidad.edu");
        reserva.setInicio(inicio);
        reserva.setFin(fin);
        reserva.setMotivo("Taller de Algoritmos");

        mockMvc.perform(post("/api/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reserva)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nombreSolicitante", is("Ing. Juan Valdés")))
                .andExpect(jsonPath("$.estado", is("CONFIRMADA")))
                .andExpect(jsonPath("$.laboratorio.id", is(laboratorio.getId().intValue())));
    }

    @Test
    @DisplayName("POST /api/reservas -> 409 Conflict cuando hay solapamiento con otra reserva existente")
    void testCrearReservaConSolapamientoHttp409() throws Exception {
        LocalDate fecha = LocalDate.now().plusDays(3);
        LocalDateTime inicio1 = fecha.atTime(14, 0);
        LocalDateTime fin1 = fecha.atTime(16, 0);

        // 1. Crear primera reserva válida
        Reserva reserva1 = new Reserva();
        reserva1.setLaboratorio(laboratorio);
        reserva1.setNombreSolicitante("Dra. Elena Gómez");
        reserva1.setCorreoSolicitante("elena.gomez@universidad.edu");
        reserva1.setInicio(inicio1);
        reserva1.setFin(fin1);
        reserva1.setMotivo("Examen de Base de Datos");

        mockMvc.perform(post("/api/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reserva1)))
                .andExpect(status().isCreated());

        // 2. Intentar crear una segunda reserva solapada (15:00 a 17:00)
        LocalDateTime inicioSolapado = fecha.atTime(15, 0);
        LocalDateTime finSolapado = fecha.atTime(17, 0);

        Reserva reservaSolapada = new Reserva();
        reservaSolapada.setLaboratorio(laboratorio);
        reservaSolapada.setNombreSolicitante("Prof. Mario Casas");
        reservaSolapada.setCorreoSolicitante("mario.casas@universidad.edu");
        reservaSolapada.setInicio(inicioSolapado);
        reservaSolapada.setFin(finSolapado);
        reservaSolapada.setMotivo("Práctica de Redes");

        mockMvc.perform(post("/api/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservaSolapada)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", containsString("ya tiene una reserva en ese horario")));
    }

    @Test
    @DisplayName("GET /api/laboratorios -> 200 OK y lista de laboratorios")
    void testListarLaboratoriosHttp200() throws Exception {
        mockMvc.perform(get("/api/laboratorios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].nombre", is("Laboratorio de Cómputo A")));
    }

    @Test
    @DisplayName("GET /api/reservas -> 200 OK y lista de reservas")
    void testListarReservasHttp200() throws Exception {
        mockMvc.perform(get("/api/reservas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("DELETE /api/reservas/{id} -> 204 No Content al cancelar reserva futura")
    void testCancelarReservaHttp204() throws Exception {
        LocalDate fecha = LocalDate.now().plusDays(4);
        LocalDateTime inicio = fecha.atTime(9, 0);
        LocalDateTime fin = fecha.atTime(11, 0);

        Reserva reserva = new Reserva();
        reserva.setLaboratorio(laboratorio);
        reserva.setNombreSolicitante("Prof. Laura");
        reserva.setCorreoSolicitante("laura@universidad.edu");
        reserva.setInicio(inicio);
        reserva.setFin(fin);

        String jsonResponse = mockMvc.perform(post("/api/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reserva)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Reserva creada = objectMapper.readValue(jsonResponse, Reserva.class);

        mockMvc.perform(delete("/api/reservas/" + creada.getId()))
                .andExpect(status().isNoContent());

        // Verificar que el estado cambió a CANCELADA
        mockMvc.perform(get("/api/reservas/" + creada.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("CANCELADA")));
    }
}
