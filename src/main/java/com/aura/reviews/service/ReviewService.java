package com.aura.reviews.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.aura.reviews.entity.Review;
import com.aura.reviews.repository.ReviewRepository;

// Servicio CRUD para Review
@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    public List<Review> listarTodos() {
        return reviewRepository.findAll();
    }
    
    public Page<Review> listarPaginado(Pageable pageable) {
        return reviewRepository.findAll(pageable);
    }
    
    public Page<Review> buscar(String busqueda, Pageable pageable) {
        if (busqueda == null || busqueda.trim().isEmpty()) {
            return reviewRepository.findAll(pageable);
        }
        return reviewRepository.buscarPorTermino(busqueda.trim(), pageable);
    }

    public Review buscarPorId(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review no encontrada con ID: " + id));
    }

    public Review guardar(Review review) {
        return reviewRepository.save(review);
    }

    public void borrar(Long id) {
        Review review = buscarPorId(id);
        reviewRepository.delete(review);
    }
    
    // Retorna un mapa valoración -> cantidad (1-5 estrellas)
    public Map<Integer, Long> contarPorValoracion() {
        List<Object[]> resultados = reviewRepository.contarPorValoracion();
        Map<Integer, Long> conteo = new HashMap<>();
        
        // Inicializar 1-5 con valor 0
        for (int i = 1; i <= 5; i++) {
            conteo.put(i, 0L);
        }
        
        // Rellenar con datos reales
        for (Object[] resultado : resultados) {
            Integer valoracion = (Integer) resultado[0];
            Long cantidad = (Long) resultado[1];
            conteo.put(valoracion, cantidad);
        }
        
        return conteo;
    }
}
