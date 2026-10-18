package br.com.filasaude.controllers;

import br.com.filasaude.entities.Fila;
import br.com.filasaude.exceptions.RegraNegocioException;
import br.com.filasaude.services.FilaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/filas")
public class FilaController {

    private final FilaService filaService;

    public FilaController(FilaService filaService) {
        this.filaService = filaService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("filas", filaService.listar());
        return "filas/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("fila", new Fila());
        return "filas/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("fila", filaService.buscarPorId(id));
        return "filas/form";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute("fila") Fila fila, BindingResult result,
                         Model model, RedirectAttributes attrs) {
        if (result.hasErrors()) {
            return "filas/form";
        }
        try {
            filaService.salvar(fila);
        } catch (RegraNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            return "filas/form";
        }
        attrs.addFlashAttribute("sucesso", "Fila salva.");
        return "redirect:/filas";
    }

    @PostMapping("/{id}/alternar-ativa")
    public String alternarAtiva(@PathVariable Long id, RedirectAttributes attrs) {
        filaService.alternarAtiva(id);
        attrs.addFlashAttribute("sucesso", "Situação da fila atualizada.");
        return "redirect:/filas";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes attrs) {
        filaService.excluir(id);
        attrs.addFlashAttribute("sucesso", "Fila excluída.");
        return "redirect:/filas";
    }
}
