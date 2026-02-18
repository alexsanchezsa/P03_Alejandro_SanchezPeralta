package com.aura.reviews.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aura.reviews.entity.Review;

// Repositorio CRUD para Review
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    // Búsqueda parcial por descripción
    @Query("SELECT r FROM Review r WHERE " +
           "LOWER(r.descripcion) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    Page<Review> buscarPorTermino(@Param("busqueda") String busqueda, Pageable pageable);
    
    Page<Review> findAll(Pageable pageable);
    
    // Cuenta reviews agrupadas por valoración (1-5)
    @Query("SELECT r.valoracion, COUNT(r) FROM Review r GROUP BY r.valoracion ORDER BY r.valoracion")
    List<Object[]> contarPorValoracion();
}
