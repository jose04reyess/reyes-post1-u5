package com.universidad.reservaslabs;

import com.universidad.reservaslabs.exception.RecursoNoEncontradoException;
import com.universidad.reservaslabs.exception.ReservaConflictException;
import com.universidad.reservaslabs.model.EstadoReserva;
import com.universidad.reservaslabs.model.Laboratorio;
import com.universidad.reservaslabs.model.Reserva;
import com.universidad.reservaslabs.repository.LaboratorioRepository;
import com.universidad.reservaslabs.repository.ReservaRepository;
import com.universidad.reservaslabs.service.ReservaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private LaboratorioRepository laboratorioRepository;

    @InjectMocks
    private ReservaService reservaService;

    private Laboratorio laboratorio;
    private LocalDate fechaFutura;

    @BeforeEach
    void setUp() {
        laboratorio = new Laboratorio(1L, "Laboratorio de Cómputo A", "Edificio 1 - Piso 2", 30, "Informática");
        fechaFutura = LocalDate.now().plusDays(2);
    }

    @Nested
    @DisplayName("Pruebas de Creación de Reservas")
    class CrearReservaTests {

        @Test
        @DisplayName("1. Creación exitosa de reserva en horario libre dentro de los límites válidos")
        void testCrearReservaExitosa() {
            LocalDateTime inicio = fechaFutura.atTime(8, 0);
            LocalDateTime fin = fechaFutura.atTime(10, 0);

            Reserva nuevaReserva = new Reserva();
            nuevaReserva.setLaboratorio(laboratorio);
            nuevaReserva.setNombreSolicitante("Prof. Carlos Rodríguez");
            nuevaReserva.setCorreoSolicitante("carlos@univ.edu");
            nuevaReserva.setInicio(inicio);
            nuevaReserva.setFin(fin);
            nuevaReserva.setMotivo("Clase de Programación");

            when(laboratorioRepository.findById(1L)).thenReturn(Optional.of(laboratorio));
            when(reservaRepository.buscarSolapamientos(eq(1L), eq(inicio), eq(fin))).thenReturn(Collections.emptyList());
            when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> {
                Reserva r = invocation.getArgument(0);
                r.setId(10L);
                return r;
            });

            Reserva creada = reservaService.crear(nuevaReserva);

            assertNotNull(creada);
            assertEquals(10L, creada.getId());
            assertEquals(EstadoReserva.CONFIRMADA, creada.getEstado());
            assertEquals(laboratorio, creada.getLaboratorio());
            verify(laboratorioRepository).findById(1L);
            verify(reservaRepository).buscarSolapamientos(1L, inicio, fin);
            verify(reservaRepository).save(nuevaReserva);
        }

        @Test
        @DisplayName("2. Lanzamiento de ReservaConflictException por solapamiento de horario")
        void testCrearReservaConSolapamientoLanzaExcepcion() {
            LocalDateTime inicio = fechaFutura.atTime(9, 0);
            LocalDateTime fin = fechaFutura.atTime(11, 0);

            Reserva nuevaReserva = new Reserva();
            nuevaReserva.setLaboratorio(laboratorio);
            nuevaReserva.setNombreSolicitante("Prof. Ana Torres");
            nuevaReserva.setCorreoSolicitante("ana@univ.edu");
            nuevaReserva.setInicio(inicio);
            nuevaReserva.setFin(fin);

            Reserva reservaExistente = new Reserva(1L, laboratorio, "Prof. Carlos", "carlos@univ.edu",
                    fechaFutura.atTime(8, 0), fechaFutura.atTime(10, 0), "Existente", EstadoReserva.CONFIRMADA);

            when(laboratorioRepository.findById(1L)).thenReturn(Optional.of(laboratorio));
            when(reservaRepository.buscarSolapamientos(eq(1L), eq(inicio), eq(fin)))
                    .thenReturn(List.of(reservaExistente));

            ReservaConflictException exception = assertThrows(
                    ReservaConflictException.class,
                    () -> reservaService.crear(nuevaReserva)
            );

            assertEquals("El laboratorio Laboratorio de Cómputo A ya tiene una reserva en ese horario", exception.getMessage());
            verify(reservaRepository, never()).save(any());
        }

        @Test
        @DisplayName("3a. Lanzamiento de ReservaConflictException por horario antes de las 07:00")
        void testCrearReservaAntesDeAperturaLanzaExcepcion() {
            LocalDateTime inicio = fechaFutura.atTime(6, 30);
            LocalDateTime fin = fechaFutura.atTime(8, 0);

            Reserva nuevaReserva = new Reserva();
            nuevaReserva.setLaboratorio(laboratorio);
            nuevaReserva.setInicio(inicio);
            nuevaReserva.setFin(fin);

            when(laboratorioRepository.findById(1L)).thenReturn(Optional.of(laboratorio));

            ReservaConflictException exception = assertThrows(
                    ReservaConflictException.class,
                    () -> reservaService.crear(nuevaReserva)
            );

            assertEquals("El horario permitido de reserva es entre las 07:00 y las 21:00", exception.getMessage());
            verify(reservaRepository, never()).buscarSolapamientos(any(), any(), any());
        }

        @Test
        @DisplayName("3b. Lanzamiento de ReservaConflictException por horario después de las 21:00")
        void testCrearReservaDespuesDeCierreLanzaExcepcion() {
            LocalDateTime inicio = fechaFutura.atTime(20, 0);
            LocalDateTime fin = fechaFutura.atTime(21, 30);

            Reserva nuevaReserva = new Reserva();
            nuevaReserva.setLaboratorio(laboratorio);
            nuevaReserva.setInicio(inicio);
            nuevaReserva.setFin(fin);

            when(laboratorioRepository.findById(1L)).thenReturn(Optional.of(laboratorio));

            ReservaConflictException exception = assertThrows(
                    ReservaConflictException.class,
                    () -> reservaService.crear(nuevaReserva)
            );

            assertEquals("El horario permitido de reserva es entre las 07:00 y las 21:00", exception.getMessage());
        }

        @Test
        @DisplayName("3c. Lanzamiento de ReservaConflictException por duración menor a 30 minutos")
        void testCrearReservaDuracionMenorA30MinLanzaExcepcion() {
            LocalDateTime inicio = fechaFutura.atTime(10, 0);
            LocalDateTime fin = fechaFutura.atTime(10, 20); // 20 minutos

            Reserva nuevaReserva = new Reserva();
            nuevaReserva.setLaboratorio(laboratorio);
            nuevaReserva.setInicio(inicio);
            nuevaReserva.setFin(fin);

            when(laboratorioRepository.findById(1L)).thenReturn(Optional.of(laboratorio));

            ReservaConflictException exception = assertThrows(
                    ReservaConflictException.class,
                    () -> reservaService.crear(nuevaReserva)
            );

            assertEquals("La duración de la reserva debe ser de mínimo 30 minutos y máximo 3 horas", exception.getMessage());
        }

        @Test
        @DisplayName("3d. Lanzamiento de ReservaConflictException por duración mayor a 3 horas")
        void testCrearReservaDuracionMayorA3HorasLanzaExcepcion() {
            LocalDateTime inicio = fechaFutura.atTime(10, 0);
            LocalDateTime fin = fechaFutura.atTime(13, 30); // 3.5 horas

            Reserva nuevaReserva = new Reserva();
            nuevaReserva.setLaboratorio(laboratorio);
            nuevaReserva.setInicio(inicio);
            nuevaReserva.setFin(fin);

            when(laboratorioRepository.findById(1L)).thenReturn(Optional.of(laboratorio));

            ReservaConflictException exception = assertThrows(
                    ReservaConflictException.class,
                    () -> reservaService.crear(nuevaReserva)
            );

            assertEquals("La duración de la reserva debe ser de mínimo 30 minutos y máximo 3 horas", exception.getMessage());
        }

        @Test
        @DisplayName("3e. Lanzamiento de ReservaConflictException cuando fin es menor o igual a inicio")
        void testCrearReservaFinMenorAInicioLanzaExcepcion() {
            LocalDateTime inicio = fechaFutura.atTime(10, 0);
            LocalDateTime fin = fechaFutura.atTime(9, 0);

            Reserva nuevaReserva = new Reserva();
            nuevaReserva.setLaboratorio(laboratorio);
            nuevaReserva.setInicio(inicio);
            nuevaReserva.setFin(fin);

            when(laboratorioRepository.findById(1L)).thenReturn(Optional.of(laboratorio));

            assertThrows(ReservaConflictException.class, () -> reservaService.crear(nuevaReserva));
        }

        @Test
        @DisplayName("3f. Lanzamiento de RecursoNoEncontradoException cuando el laboratorio no existe")
        void testCrearReservaLaboratorioNoExiste() {
            Reserva nuevaReserva = new Reserva();
            Laboratorio labInexistente = new Laboratorio();
            labInexistente.setId(999L);
            nuevaReserva.setLaboratorio(labInexistente);

            when(laboratorioRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(RecursoNoEncontradoException.class, () -> reservaService.crear(nuevaReserva));
        }
    }

    @Nested
    @DisplayName("Pruebas de Cancelación de Reservas")
    class CancelarReservaTests {

        @Test
        @DisplayName("4. Lanzamiento de ReservaConflictException al cancelar reserva cuyo horario de inicio ya pasó")
        void testCancelarReservaPasadaLanzaExcepcion() {
            LocalDateTime inicioPasado = LocalDateTime.now().minusHours(2);
            LocalDateTime finPasado = LocalDateTime.now().minusHours(1);

            Reserva reservaPasada = new Reserva(5L, laboratorio, "Prof. Carlos", "carlos@univ.edu",
                    inicioPasado, finPasado, "Clase pasada", EstadoReserva.CONFIRMADA);

            when(reservaRepository.findById(5L)).thenReturn(Optional.of(reservaPasada));

            ReservaConflictException exception = assertThrows(
                    ReservaConflictException.class,
                    () -> reservaService.cancelar(5L)
            );

            assertEquals("No se puede cancelar una reserva cuyo horario de inicio ya pasó", exception.getMessage());
            verify(reservaRepository, never()).save(any());
        }

        @Test
        @DisplayName("5. Cancelación exitosa de una reserva futura")
        void testCancelarReservaFuturaExitosa() {
            LocalDateTime inicioFuturo = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
            LocalDateTime finFuturo = LocalDateTime.now().plusDays(1).withHour(12).withMinute(0);

            Reserva reservaFutura = new Reserva(5L, laboratorio, "Prof. Carlos", "carlos@univ.edu",
                    inicioFuturo, finFuturo, "Clase futura", EstadoReserva.CONFIRMADA);

            when(reservaRepository.findById(5L)).thenReturn(Optional.of(reservaFutura));
            when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Reserva cancelada = reservaService.cancelar(5L);

            assertNotNull(cancelada);
            assertEquals(EstadoReserva.CANCELADA, cancelada.getEstado());
            verify(reservaRepository).save(reservaFutura);
        }

        @Test
        @DisplayName("6. Lanzamiento de RecursoNoEncontradoException al cancelar reserva inexistente")
        void testCancelarReservaInexistente() {
            when(reservaRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(RecursoNoEncontradoException.class, () -> reservaService.cancelar(999L));
        }
    }
}
