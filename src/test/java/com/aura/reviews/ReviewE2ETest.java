package com.aura.reviews;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
import com.aura.reviews.entity.Review;
import com.aura.reviews.repository.ClienteRepository;
import com.aura.reviews.repository.ReviewRepository;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
class ReviewE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ClienteRepository clienteRepository;


    @BeforeEach
    void limpiarBaseDatos() {
        reviewRepository.deleteAll();
        clienteRepository.deleteAll();
    }

    @Test
    @DisplayName("TC-E03: Flujo completo de review vinculada a cliente vía HTTP real")
    void flujoCompletoReviewConCliente() {
        // Crear cliente
        Cliente nuevoCliente = new Cliente("Marta Sánchez", 35, "Femenino", false, null);

        ResponseEntity<Cliente> respuestaCliente = restTemplate.postForEntity(
                "/api/clientes", nuevoCliente, Cliente.class);

        assertEquals(HttpStatus.CREATED, respuestaCliente.getStatusCode());
        Cliente clienteCreado = respuestaCliente.getBody();
        assertNotNull(clienteCreado);
        Long clienteId = clienteCreado.getId();

        // Crear review vinculada al cliente
        Review nuevaReview = new Review();
        nuevaReview.setDescripcion("Experiencia gastronómica excepcional, volveremos seguro");
        nuevaReview.setValoracion(5);
        Cliente refCliente = new Cliente();
        refCliente.setId(clienteId);
        nuevaReview.setCliente(refCliente);

        ResponseEntity<Review> respuestaReview = restTemplate.postForEntity(
                "/api/reviews", nuevaReview, Review.class);

        assertEquals(HttpStatus.CREATED, respuestaReview.getStatusCode());
        Review reviewCreada = respuestaReview.getBody();
        assertNotNull(reviewCreada);
        assertNotNull(reviewCreada.getId());
        assertEquals(5, reviewCreada.getValoracion());
        assertEquals("Experiencia gastronómica excepcional, volveremos seguro",
                reviewCreada.getDescripcion());
        Long reviewId = reviewCreada.getId();

        // Leer review y verificar relación con cliente
        ResponseEntity<Review> respuestaLeer = restTemplate.getForEntity(
                "/api/reviews/" + reviewId, Review.class);

        assertEquals(HttpStatus.OK, respuestaLeer.getStatusCode());
        Review reviewLeida = respuestaLeer.getBody();
        assertNotNull(reviewLeida);
        assertNotNull(reviewLeida.getCliente());
        assertEquals(clienteId, reviewLeida.getCliente().getId());
        assertEquals("Marta Sánchez", reviewLeida.getCliente().getNombre());

        // Actualizar valoración
        Review datosActualizados = new Review();
        datosActualizados.setDescripcion("Tras una segunda visita, la experiencia fue buena pero no excepcional");
        datosActualizados.setValoracion(3);
        datosActualizados.setCliente(refCliente);

        ResponseEntity<Review> respuestaActualizar = restTemplate.exchange(
                "/api/reviews/" + reviewId, HttpMethod.PUT,
                new HttpEntity<>(datosActualizados), Review.class);

        assertEquals(HttpStatus.OK, respuestaActualizar.getStatusCode());
        Review reviewActualizada = respuestaActualizar.getBody();
        assertNotNull(reviewActualizada);
        assertEquals(3, reviewActualizada.getValoracion());

        // Eliminar review
        ResponseEntity<Void> respuestaEliminar = restTemplate.exchange(
                "/api/reviews/" + reviewId, HttpMethod.DELETE,
                null, Void.class);

        assertEquals(HttpStatus.NO_CONTENT, respuestaEliminar.getStatusCode());

        // Verificar que el cliente sigue existiendo
        ResponseEntity<Cliente> respuestaClienteExiste = restTemplate.getForEntity(
                "/api/clientes/" + clienteId, Cliente.class);

        assertEquals(HttpStatus.OK, respuestaClienteExiste.getStatusCode());
        assertEquals("Marta Sánchez", respuestaClienteExiste.getBody().getNombre());
    }

    // Múltiples reviews vinculadas a distintos clientes
    @Test
    @DisplayName("TC-E04: Múltiples reviews vinculadas a distintos clientes")
    void multiplesReviewsConDistintosClientes() {
        // Crear 2 clientes
        ResponseEntity<Cliente> resp1 = restTemplate.postForEntity("/api/clientes",
                new Cliente("Andrés Molina", 28, "Masculino", false, null), Cliente.class);
        ResponseEntity<Cliente> resp2 = restTemplate.postForEntity("/api/clientes",
                new Cliente("Isabel Romero", 45, "Femenino", true, "Frutos secos"), Cliente.class);

        Long idCliente1 = resp1.getBody().getId();
        Long idCliente2 = resp2.getBody().getId();

        // Crear 1 review para cada cliente
        Review review1 = new Review();
        review1.setDescripcion("Ambiente agradable pero comida mejorable");
        review1.setValoracion(3);
        Cliente ref1 = new Cliente();
        ref1.setId(idCliente1);
        review1.setCliente(ref1);

        Review review2 = new Review();
        review2.setDescripcion("Todo perfecto, carta adaptada para intolerancias");
        review2.setValoracion(5);
        Cliente ref2 = new Cliente();
        ref2.setId(idCliente2);
        review2.setCliente(ref2);

        restTemplate.postForEntity("/api/reviews", review1, Review.class);
        restTemplate.postForEntity("/api/reviews", review2, Review.class);

        // Verificar listado completo
        ResponseEntity<Review[]> respuestaLista = restTemplate.getForEntity(
                "/api/reviews", Review[].class);

        assertEquals(HttpStatus.OK, respuestaLista.getStatusCode());
        Review[] reviews = respuestaLista.getBody();
        assertNotNull(reviews);
        assertEquals(2, reviews.length);

        // Verificar vinculación correcta
        for (Review r : reviews) {
            assertNotNull(r.getCliente(), "Cada review debe tener un cliente vinculado");
            if (r.getValoracion() == 3) {
                assertEquals("Andrés Molina", r.getCliente().getNombre());
                assertEquals("Ambiente agradable pero comida mejorable", r.getDescripcion());
            } else if (r.getValoracion() == 5) {
                assertEquals("Isabel Romero", r.getCliente().getNombre());
                assertEquals("Todo perfecto, carta adaptada para intolerancias", r.getDescripcion());
            }
        }
    }
}
