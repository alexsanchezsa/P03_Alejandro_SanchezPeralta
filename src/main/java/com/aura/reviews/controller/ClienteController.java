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
import com.aura.reviews.service.ClienteService;
import com.aura.reviews.service.ReviewService;

import jakarta.validation.Valid;

// Controlador MVC para CRUD de clientes
@Controller
@RequestMapping("/clientes")
public class ClienteController {

    private static final int TAMANO_PAGINA = 5;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ReviewService reviewService;

    @GetMapping
    public String listar(@RequestParam(defaultValue = "0") int page,
                         @RequestParam(required = false) String busqueda,
                         Model model) {
        Pageable pageable = PageRequest.of(page, TAMANO_PAGINA, Sort.by("id").descending());
        Page<Cliente> paginaClientes;
        
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            paginaClientes = clienteService.buscar(busqueda, pageable);
            model.addAttribute("busqueda", busqueda);
        } else {
            paginaClientes = clienteService.listarPaginado(pageable);
        }
        
        model.addAttribute("clientes", paginaClientes.getContent());
        model.addAttribute("paginaActual", page);
        model.addAttribute("totalPaginas", paginaClientes.getTotalPages());
        model.addAttribute("totalElementos", paginaClientes.getTotalElements());
        return "clientes/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        Cliente cliente = new Cliente();
        model.addAttribute("cliente", cliente);
        return "clientes/form";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Cliente cliente = clienteService.buscarPorId(id);
        model.addAttribute("cliente", cliente);
        return "clientes/form";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("cliente") Cliente cliente,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "clientes/form";
        }
        
        // Limpiar detalle si no tiene intolerancia
        if (cliente.getIntolerancia() == null || !cliente.getIntolerancia()) {
            cliente.setDetalleIntolerancia(null);
        }
        
        clienteService.guardar(cliente);
        redirectAttributes.addFlashAttribute("mensaje", "Cliente guardado correctamente");
        return "redirect:/clientes";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Cliente cliente = clienteService.buscarPorId(id);
        
        // Desvincular review si existe
        if (cliente.getReview() != null) {
            reviewService.borrar(cliente.getReview().getId());
        }
        
        clienteService.borrar(id);
        redirectAttributes.addFlashAttribute("mensaje", "Cliente eliminado correctamente");
        return "redirect:/clientes";
    }
}
