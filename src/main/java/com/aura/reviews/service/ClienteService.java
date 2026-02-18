package com.aura.reviews.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.aura.reviews.entity.Cliente;
import com.aura.reviews.repository.ClienteRepository;

// Servicio CRUD para Cliente
@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }
    
    public Page<Cliente> listarPaginado(Pageable pageable) {
        return clienteRepository.findAll(pageable);
    }
    
    public Page<Cliente> buscar(String busqueda, Pageable pageable) {
        if (busqueda == null || busqueda.trim().isEmpty()) {
            return clienteRepository.findAll(pageable);
        }
        return clienteRepository.buscarPorTermino(busqueda.trim(), pageable);
    }

    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + id));
    }

    public Cliente guardar(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    // Elimina cliente (y su review por cascade)
    public void borrar(Long id) {
        Cliente cliente = buscarPorId(id);
        clienteRepository.delete(cliente);
    }
}
