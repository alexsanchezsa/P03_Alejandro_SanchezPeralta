package com.aura.reviews.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aura.reviews.entity.Cliente;
import com.aura.reviews.entity.Review;
import com.aura.reviews.service.ClienteService;
import com.aura.reviews.service.ReviewService;

import jakarta.validation.Valid;

// API REST para reviews (/api/reviews)
@RestController
@RequestMapping("/api/reviews")
public class ReviewRestController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ClienteService clienteService;

    @GetMapping
    public ResponseEntity<List<Review>> listarTodas() {
        return ResponseEntity.ok(reviewService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Review> obtenerPorId(@PathVariable Long id) {
        try {
            Review review = reviewService.buscarPorId(id);
            return ResponseEntity.ok(review);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Review> crear(@Valid @RequestBody Review review) {
        vincularCliente(review);
        Review guardada = reviewService.guardar(review);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Review> actualizar(@PathVariable Long id,
                                             @Valid @RequestBody Review review) {
        try {
            reviewService.buscarPorId(id);
            review.setId(id);
            vincularCliente(review);
            return ResponseEntity.ok(reviewService.guardar(review));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        try {
            reviewService.borrar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Vincula la referencia de cliente (por ID) con la entidad completa
    private void vincularCliente(Review review) {
        if (review.getCliente() != null && review.getCliente().getId() != null) {
            Cliente cliente = clienteService.buscarPorId(review.getCliente().getId());
            review.setCliente(cliente);
        }
    }
}
