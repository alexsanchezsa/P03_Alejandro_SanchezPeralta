package com.aura.reviews.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// Redirige la raíz (/) al listado de clientes
@Controller
public class HomeController {

    @GetMapping("/")
    public String inicio() {
        return "redirect:/clientes";
    }
}
