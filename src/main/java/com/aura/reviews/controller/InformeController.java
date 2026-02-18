package com.aura.reviews.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.aura.reviews.entity.Cliente;
import com.aura.reviews.service.ClienteService;
import com.aura.reviews.service.ReviewService;

// Controlador de informes con estadísticas para Chart.js
@Controller
@RequestMapping("/informes")
public class InformeController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ReviewService reviewService;

    @GetMapping
    public String mostrarInformes(Model model) {
        // Clientes por género
        Map<String, Long> clientesPorGenero = obtenerClientesPorGenero();
        model.addAttribute("clientesPorGenero", clientesPorGenero);
        
        // Reviews por estrellas
        Map<Integer, Long> reviewsPorEstrellas = reviewService.contarPorValoracion();
        model.addAttribute("reviewsPorEstrellas", reviewsPorEstrellas);
        
        // Clientes por edad
        Map<String, Long> clientesPorEdad = obtenerClientesPorFranjasEdad();
        model.addAttribute("clientesPorEdad", clientesPorEdad);
        
        // Clientes con/sin intolerancia
        Map<String, Long> clientesPorIntolerancia = obtenerClientesPorIntolerancia();
        model.addAttribute("clientesPorIntolerancia", clientesPorIntolerancia);
        
        return "informes/dashboard";
    }

    private Map<String, Long> obtenerClientesPorGenero() {
        List<Cliente> clientes = clienteService.listarTodos();
        Map<String, Long> conteo = new HashMap<>();
        
        for (Cliente cliente : clientes) {
            String genero = cliente.getGenero();
            conteo.put(genero, conteo.getOrDefault(genero, 0L) + 1);
        }
        
        return conteo;
    }

    // Franjas: 0-15, 15-24, 25-35, 36-50, 51-65, 66+
    private Map<String, Long> obtenerClientesPorFranjasEdad() {
        List<Cliente> clientes = clienteService.listarTodos();
        Map<String, Long> conteo = new HashMap<>();
        
        conteo.put("0-15", 0L);
        conteo.put("15-24", 0L);
        conteo.put("25-35", 0L);
        conteo.put("36-50", 0L);
        conteo.put("51-65", 0L);
        conteo.put("66+", 0L);
        
        for (Cliente cliente : clientes) {
            int edad = cliente.getEdad();
            String franja;
            
            if (edad <= 15) {
                franja = "0-15";
            } else if (edad <= 24) {
                franja = "15-24";
            } else if (edad <= 35) {
                franja = "25-35";
            } else if (edad <= 50) {
                franja = "36-50";
            } else if (edad <= 65) {
                franja = "51-65";
            } else {
                franja = "66+";
            }
            
            conteo.put(franja, conteo.get(franja) + 1);
        }
        
        return conteo;
    }

    // Porcentaje de clientes con necesidades alimentarias especiales
    private Map<String, Long> obtenerClientesPorIntolerancia() {
        List<Cliente> clientes = clienteService.listarTodos();
        Map<String, Long> conteo = new HashMap<>();
        
        conteo.put("Con intolerancia", 0L);
        conteo.put("Sin intolerancia", 0L);
        
        for (Cliente cliente : clientes) {
            if (cliente.getIntolerancia() != null && cliente.getIntolerancia()) {
                conteo.put("Con intolerancia", conteo.get("Con intolerancia") + 1);
            } else {
                conteo.put("Sin intolerancia", conteo.get("Sin intolerancia") + 1);
            }
        }
        
        return conteo;
    }
}
