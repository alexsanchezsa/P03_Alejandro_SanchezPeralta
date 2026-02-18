package com.aura.reviews;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aura.reviews.entity.Cliente;
import com.aura.reviews.entity.Review;
import com.aura.reviews.repository.ReviewRepository;
import com.aura.reviews.service.ReviewService;

// Tests unitarios de ReviewService con Mockito (sin contexto Spring)
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewService reviewService;

    // contarPorValoracion devuelve mapa 1-5 con los conteos correctos
    @Test
    @DisplayName("TC-U04: contarPorValoracion() debe retornar mapa completo de 1 a 5 estrellas")
    void contarPorValoracion_debeRetornarMapaCompletoConTodasLasValoraciones() {
        // Arrange - 2 reviews de 3 estrellas y 3 de 5 estrellas
        List<Object[]> datosSimulados = Arrays.asList(
                new Object[]{3, 2L},
                new Object[]{5, 3L}
        );
        when(reviewRepository.contarPorValoracion()).thenReturn(datosSimulados);

        // Act
        Map<Integer, Long> resultado = reviewService.contarPorValoracion();

        // Assert - El mapa debe tener 5 claves con los valores correctos
        assertNotNull(resultado);
        assertEquals(5, resultado.size(), "El mapa debe contener exactamente 5 entradas (1-5)");
        assertEquals(0L, resultado.get(1), "Valoración 1 debe ser 0");
        assertEquals(0L, resultado.get(2), "Valoración 2 debe ser 0");
        assertEquals(2L, resultado.get(3), "Valoración 3 debe ser 2");
        assertEquals(0L, resultado.get(4), "Valoración 4 debe ser 0");
        assertEquals(3L, resultado.get(5), "Valoración 5 debe ser 3");

        verify(reviewRepository).contarPorValoracion();
    }

    // Guardar review con cliente y luego borrarla
    @Test
    @DisplayName("TC-U05: guardar() debe persistir la review y borrar() debe eliminarla")
    void guardarYBorrarReview_flujoCorrecto() {
        // Arrange
        Cliente cliente = new Cliente("Pedro Ruiz", 45, "Masculino", false, null);
        cliente.setId(1L);

        Review reviewNueva = new Review("Excelente servicio y atención al cliente", 5, cliente);
        Review reviewGuardada = new Review("Excelente servicio y atención al cliente", 5, cliente);
        reviewGuardada.setId(1L);

        when(reviewRepository.save(any(Review.class))).thenReturn(reviewGuardada);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(reviewGuardada));

        // Act - Guardar
        Review resultado = reviewService.guardar(reviewNueva);

        // Assert - Verificar guardado
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Excelente servicio y atención al cliente", resultado.getDescripcion());
        assertEquals(5, resultado.getValoracion());
        assertEquals("Pedro Ruiz", resultado.getCliente().getNombre());
        verify(reviewRepository).save(reviewNueva);

        // Borrar y verificar
        reviewService.borrar(1L);

        verify(reviewRepository).delete(reviewGuardada);
    }

    // buscarPorId con ID inexistente lanza excepción
    @Test
    @DisplayName("TC-U06: buscarPorId() con ID inexistente debe lanzar excepción")
    void buscarPorId_reviewNoExiste_debeLanzarExcepcion() {
        // Arrange
        when(reviewRepository.findById(404L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.buscarPorId(404L)
        );

        assertEquals("Review no encontrada con ID: 404", excepcion.getMessage());
        verify(reviewRepository).findById(404L);
    }
}
