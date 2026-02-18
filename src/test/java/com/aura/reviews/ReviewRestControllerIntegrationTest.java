package com.aura.reviews;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.aura.reviews.entity.Cliente;
import com.aura.reviews.entity.Review;
import com.aura.reviews.repository.ClienteRepository;
import com.aura.reviews.repository.ReviewRepository;


// Tests de integración para /api/reviews con MockMvc y H2
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReviewRestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    // Limpia BD antes de cada test para evitar datos residuales
    @BeforeEach
    void limpiarBaseDatos() {
        reviewRepository.deleteAll();
        clienteRepository.deleteAll();
    }

    // GET lista reviews y POST crea una nueva vinculada a cliente
    @Test
    @DisplayName("TC-I04: GET /api/reviews lista las reviews y POST crea una nueva vinculada a cliente")
    void listarYCrearReview_conCliente() throws Exception {
        // Crear cliente para asociar
        Cliente cliente = clienteRepository.save(
                new Cliente("Sara Gómez", 27, "Femenino", false, null));

        // POST con datos válidos -> 201
        String reviewJson = String.format(
                "{\"descripcion\": \"Comida deliciosa y servicio rápido\", " +
                "\"valoracion\": 4, " +
                "\"cliente\": {\"id\": %d}}", cliente.getId());

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.descripcion", is("Comida deliciosa y servicio rápido")))
                .andExpect(jsonPath("$.valoracion", is(4)))
                .andExpect(jsonPath("$.cliente.id", is(cliente.getId().intValue())))
                .andExpect(jsonPath("$.cliente.nombre", is("Sara Gómez")));

        // GET -> 200 con 1 review
        mockMvc.perform(get("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].descripcion", is("Comida deliciosa y servicio rápido")))
                .andExpect(jsonPath("$[0].valoracion", is(4)));
    }

    // Validación 400, actualización 200 y eliminación 204
    @Test
    @DisplayName("TC-I05: Validación 400, actualización 200 y eliminación 204 de reviews")
    void validacionActualizacionYEliminacion_reviews() throws Exception {
        // POST inválido -> 400
        String reviewInvalida = "{\"descripcion\": \"\", \"valoracion\": 0}";

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewInvalida))
                .andExpect(status().isBadRequest());

        // Preparar datos para PUT
        Cliente cliente = clienteRepository.save(
                new Cliente("Diego Martín", 40, "Masculino", false, null));
        Review review = new Review("Review original", 2, cliente);
        review = reviewRepository.save(review);
        Long reviewId = review.getId();

        // PUT -> 200
        String reviewActualizada = String.format(
                "{\"descripcion\": \"Review actualizada con mejor opinión\", " +
                "\"valoracion\": 4, " +
                "\"cliente\": {\"id\": %d}}", cliente.getId());

        mockMvc.perform(put("/api/reviews/" + reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewActualizada))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion", is("Review actualizada con mejor opinión")))
                .andExpect(jsonPath("$.valoracion", is(4)));

        // DELETE -> 204
        mockMvc.perform(delete("/api/reviews/" + reviewId))
                .andExpect(status().isNoContent());

        // Verificar eliminación
        mockMvc.perform(get("/api/reviews/" + reviewId))
                .andExpect(status().isNotFound());
    }
}
