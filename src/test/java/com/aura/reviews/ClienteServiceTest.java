package com.aura.reviews;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aura.reviews.entity.Cliente;
import com.aura.reviews.repository.ClienteRepository;
import com.aura.reviews.service.ClienteService;

// Tests unitarios de ClienteService con Mockito (sin contexto Spring)
@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    // Guardar cliente y verificar que se persiste con ID
    @Test
    @DisplayName("TC-U01: guardar() debe persistir el cliente y retornar la entidad con ID")
    void guardarCliente_debeRetornarClienteConId() {
        // Arrange
        Cliente clienteNuevo = new Cliente("Laura García", 28, "Femenino", false, null);

        Cliente clienteGuardado = new Cliente("Laura García", 28, "Femenino", false, null);
        clienteGuardado.setId(1L);

        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteGuardado);

        // Act
        Cliente resultado = clienteService.guardar(clienteNuevo);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Laura García", resultado.getNombre());
        assertEquals(28, resultado.getEdad());
        assertEquals("Femenino", resultado.getGenero());
        assertEquals(false, resultado.getIntolerancia());

        verify(clienteRepository).save(clienteNuevo);
    }

    // buscarPorId con ID inexistente lanza excepción
    @Test
    @DisplayName("TC-U02: buscarPorId() con ID inexistente debe lanzar IllegalArgumentException")
    void buscarPorId_clienteNoExiste_debeLanzarExcepcion() {
        // Arrange
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> clienteService.buscarPorId(999L)
        );

        assertEquals("Cliente no encontrado con ID: 999", excepcion.getMessage());
        verify(clienteRepository).findById(999L);
    }

    // listarTodos retorna la lista completa del repositorio
    @Test
    @DisplayName("TC-U03: listarTodos() debe retornar la lista completa de clientes")
    void listarTodos_debeRetornarListaCompleta() {
        // Arrange
        Cliente c1 = new Cliente("Carlos López", 35, "Masculino", false, null);
        Cliente c2 = new Cliente("Ana Martín", 22, "Femenino", true, "Gluten");
        when(clienteRepository.findAll()).thenReturn(Arrays.asList(c1, c2));

        // Act
        List<Cliente> resultado = clienteService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Carlos López", resultado.get(0).getNombre());
        assertEquals("Ana Martín", resultado.get(1).getNombre());
        verify(clienteRepository).findAll();
    }
}
