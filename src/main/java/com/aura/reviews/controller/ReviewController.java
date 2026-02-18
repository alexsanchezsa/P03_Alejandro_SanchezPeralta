package com.aura.reviews.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.aura.reviews.entity.Cliente;
import com.aura.reviews.entity.Review;
import com.aura.reviews.service.ClienteService;
import com.aura.reviews.service.ReviewService;

import jakarta.validation.Valid;

// Controlador MVC para CRUD de reviews
@Controller
@RequestMapping("/reviews")
public class ReviewController {

    private static final int TAMANO_PAGINA = 5;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ClienteService clienteService;

    @GetMapping
    public String listar(@RequestParam(defaultValue = "0") int page,
                         @RequestParam(required = false) String busqueda,
                         Model model) {
        Pageable pageable = PageRequest.of(page, TAMANO_PAGINA, Sort.by("id").descending());
        Page<Review> paginaReviews;
        
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            paginaReviews = reviewService.buscar(busqueda, pageable);
            model.addAttribute("busqueda", busqueda);
        } else {
            paginaReviews = reviewService.listarPaginado(pageable);
        }
        
        model.addAttribute("reviews", paginaReviews.getContent());
        model.addAttribute("paginaActual", page);
        model.addAttribute("totalPaginas", paginaReviews.getTotalPages());
        model.addAttribute("totalElementos", paginaReviews.getTotalElements());
        return "reviews/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        Review review = new Review();
        review.setCliente(new Cliente());
        model.addAttribute("review", review);
        cargarClientes(model);
        return "reviews/form";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Review review = reviewService.buscarPorId(id);
        if (review.getCliente() == null) {
            review.setCliente(new Cliente());
        }
        model.addAttribute("review", review);
        cargarClientes(model);
        return "reviews/form";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("review") Review review,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            cargarClientes(model);
            return "reviews/form";
        }
        
        // Vincular cliente seleccionado
        if (review.getCliente() != null && review.getCliente().getId() != null) {
            Cliente clienteSeleccionado = clienteService.buscarPorId(review.getCliente().getId());
            review.setCliente(clienteSeleccionado);
        } else {
            review.setCliente(null);
        }
        
        reviewService.guardar(review);
        redirectAttributes.addFlashAttribute("mensaje", "Review guardada correctamente");
        return "redirect:/reviews";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        reviewService.borrar(id);
        redirectAttributes.addFlashAttribute("mensaje", "Review eliminada correctamente");
        return "redirect:/reviews";
    }

    private void cargarClientes(Model model) {
        model.addAttribute("clientes", clienteService.listarTodos());
    }
}
