package com.aura.reviews.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aura.reviews.entity.Cliente;

// Repositorio CRUD para Cliente
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    
    // Busca clientes por nombre o género
    @Query("SELECT c FROM Cliente c WHERE " +
           "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(c.genero) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    Page<Cliente> buscarPorTermino(@Param("busqueda") String busqueda, Pageable pageable);
    
    Page<Cliente> findAll(Pageable pageable);
}
