package com.aura.reviews;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.aura.reviews.entity.Cliente;
import com.aura.reviews.repository.ClienteRepository;
import com.aura.reviews.repository.ReviewRepository;

// Tests E2E de Cliente con servidor HTTP real y TestRestTemplate
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
class ClienteE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    // Limpia BD antes de cada test (reviews primero por FK)
    @BeforeEach
    void limpiarBaseDatos() {
        reviewRepository.deleteAll();
        clienteRepository.deleteAll();
    }

    // Flujo CRUD completo: crear, leer, actualizar, eliminar y verificar
    @Test
    @DisplayName("TC-E01: Flujo CRUD completo de cliente vía HTTP real")
    void flujoCrudCompletoCliente() {
        // Crear cliente
        Cliente nuevoCliente = new Cliente("Lucía Fernández", 29, "Femenino", false, null);

        ResponseEntity<Cliente> respuestaCrear = restTemplate.postForEntity(
                "/api/clientes", nuevoCliente, Cliente.class);

        assertEquals(HttpStatus.CREATED, respuestaCrear.getStatusCode());
        Cliente clienteCreado = respuestaCrear.getBody();
        assertNotNull(clienteCreado);
        assertNotNull(clienteCreado.getId());
        assertEquals("Lucía Fernández", clienteCreado.getNombre());
        assertEquals(29, clienteCreado.getEdad());
        Long clienteId = clienteCreado.getId();

        // Leer cliente por ID
        ResponseEntity<Cliente> respuestaLeer = restTemplate.getForEntity(
                "/api/clientes/" + clienteId, Cliente.class);

        assertEquals(HttpStatus.OK, respuestaLeer.getStatusCode());
        Cliente clienteLeido = respuestaLeer.getBody();
        assertNotNull(clienteLeido);
        assertEquals("Lucía Fernández", clienteLeido.getNombre());
        assertEquals("Femenino", clienteLeido.getGenero());

        // Actualizar cliente
        Cliente datosActualizados = new Cliente("Lucía Fernández García", 30, "Femenino", true, "Mariscos");

        ResponseEntity<Cliente> respuestaActualizar = restTemplate.exchange(
                "/api/clientes/" + clienteId, HttpMethod.PUT,
                new HttpEntity<>(datosActualizados), Cliente.class);

        assertEquals(HttpStatus.OK, respuestaActualizar.getStatusCode());
        Cliente clienteActualizado = respuestaActualizar.getBody();
        assertNotNull(clienteActualizado);
        assertEquals("Lucía Fernández García", clienteActualizado.getNombre());
        assertEquals(30, clienteActualizado.getEdad());
        assertEquals(true, clienteActualizado.getIntolerancia());
        assertEquals("Mariscos", clienteActualizado.getDetalleIntolerancia());

        // Eliminar cliente
        ResponseEntity<Void> respuestaEliminar = restTemplate.exchange(
                "/api/clientes/" + clienteId, HttpMethod.DELETE,
                null, Void.class);

        assertEquals(HttpStatus.NO_CONTENT, respuestaEliminar.getStatusCode());

        // Verificar que ya no existe
        ResponseEntity<Cliente> respuestaNoExiste = restTemplate.getForEntity(
                "/api/clientes/" + clienteId, Cliente.class);

        assertEquals(HttpStatus.NOT_FOUND, respuestaNoExiste.getStatusCode());
    }

    // Listado de múltiples clientes creados por HTTP
    @Test
    @DisplayName("TC-E02: Listado de múltiples clientes creados por HTTP real")
    void listadoMultiplesClientes() {
        // Verificar lista vacía
        ResponseEntity<Cliente[]> respuestaVacia = restTemplate.getForEntity(
                "/api/clientes", Cliente[].class);

        assertEquals(HttpStatus.OK, respuestaVacia.getStatusCode());
        assertEquals(0, respuestaVacia.getBody().length);

        // Crear 3 clientes
        restTemplate.postForEntity("/api/clientes",
                new Cliente("Alberto Ruiz", 50, "Masculino", false, null), Cliente.class);
        restTemplate.postForEntity("/api/clientes",
                new Cliente("Carmen Vega", 22, "Femenino", true, "Lactosa"), Cliente.class);
        restTemplate.postForEntity("/api/clientes",
                new Cliente("Pablo Serrano", 38, "Masculino", false, null), Cliente.class);

        // Verificar listado completo
        ResponseEntity<Cliente[]> respuestaLista = restTemplate.getForEntity(
                "/api/clientes", Cliente[].class);

        assertEquals(HttpStatus.OK, respuestaLista.getStatusCode());
        Cliente[] clientes = respuestaLista.getBody();
        assertNotNull(clientes);
        assertEquals(3, clientes.length);

        // Verificar que los nombres coinciden
        List<String> nombres = List.of(clientes[0].getNombre(),
                clientes[1].getNombre(), clientes[2].getNombre());
        assertTrue(nombres.contains("Alberto Ruiz"));
        assertTrue(nombres.contains("Carmen Vega"));
        assertTrue(nombres.contains("Pablo Serrano"));
    }
}
