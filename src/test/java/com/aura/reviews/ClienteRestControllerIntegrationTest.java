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
import com.aura.reviews.repository.ClienteRepository;
import com.aura.reviews.repository.ReviewRepository;
import tools.jackson.databind.ObjectMapper;

// Tests de integración para /api/clientes con MockMvc y H2
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ClienteRestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @BeforeEach
    void limpiarBaseDatos() {
        reviewRepository.deleteAll();
        clienteRepository.deleteAll();
    }

    // GET /api/clientes retorna 200 con lista correcta
    @Test
    @DisplayName("TC-I01: GET /api/clientes debe retornar 200 con la lista de clientes")
    void listarClientes_conDatos_retorna200YListaCorrecta() throws Exception {
        // Insertar datos de prueba
        clienteRepository.save(new Cliente("María López", 30, "Femenino", false, null));
        clienteRepository.save(new Cliente("Juan Torres", 25, "Masculino", true, "Lactosa"));

        // Act & Assert
        mockMvc.perform(get("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre", is("María López")))
                .andExpect(jsonPath("$[0].edad", is(30)))
                .andExpect(jsonPath("$[0].genero", is("Femenino")))
                .andExpect(jsonPath("$[1].nombre", is("Juan Torres")))
                .andExpect(jsonPath("$[1].intolerancia", is(true)))
                .andExpect(jsonPath("$[1].detalleIntolerancia", is("Lactosa")));
    }

    // POST con datos válidos -> 201, con inválidos -> 400
    @Test
    @DisplayName("TC-I02: POST /api/clientes - 201 con datos válidos, 400 con datos inválidos")
    void crearCliente_validacionCompleta() throws Exception {
        // Datos válidos -> 201
        Cliente clienteValido = new Cliente("Elena Navarro", 42, "Femenino", true, "Gluten");

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteValido)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre", is("Elena Navarro")))
                .andExpect(jsonPath("$.edad", is(42)))
                .andExpect(jsonPath("$.genero", is("Femenino")))
                .andExpect(jsonPath("$.intolerancia", is(true)))
                .andExpect(jsonPath("$.detalleIntolerancia", is("Gluten")));

        // Datos inválidos -> 400
        Cliente clienteInvalido = new Cliente("", -5, "", false, null);

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteInvalido)))
                .andExpect(status().isBadRequest());
    }

    // PUT actualiza y DELETE elimina correctamente
    @Test
    @DisplayName("TC-I03: PUT actualiza el cliente y DELETE lo elimina correctamente")
    void actualizarYEliminarCliente() throws Exception {
        // Insertar cliente
        Cliente original = clienteRepository.save(
                new Cliente("Raúl Díaz", 33, "Masculino", false, null));
        Long clienteId = original.getId();

        // PUT - actualizar
        Cliente actualizado = new Cliente("Raúl Díaz Modificado", 34, "Masculino", true, "Frutos secos");

        mockMvc.perform(put("/api/clientes/" + clienteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("Raúl Díaz Modificado")))
                .andExpect(jsonPath("$.edad", is(34)))
                .andExpect(jsonPath("$.intolerancia", is(true)))
                .andExpect(jsonPath("$.detalleIntolerancia", is("Frutos secos")));

        // DELETE
        mockMvc.perform(delete("/api/clientes/" + clienteId))
                .andExpect(status().isNoContent());

        // Verificar que ya no existe
        mockMvc.perform(get("/api/clientes/" + clienteId))
                .andExpect(status().isNotFound());
    }
}
