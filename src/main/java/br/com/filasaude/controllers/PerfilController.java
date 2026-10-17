package br.com.filasaude.controllers;

import br.com.filasaude.entities.Perfil;
import br.com.filasaude.exceptions.RegraNegocioException;
import br.com.filasaude.services.PerfilService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/perfis")
public class PerfilController {

    private final PerfilService perfilService;

    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("perfis", perfilService.listar());
        return "perfis/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("perfil", new Perfil());
        return "perfis/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("perfil", perfilService.buscarPorId(id));
        return "perfis/form";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute("perfil") Perfil perfil, BindingResult result,
                         Model model, RedirectAttributes attrs) {
        if (result.hasErrors()) {
            return "perfis/form";
        }
        try {
            perfilService.salvar(perfil);
        } catch (RegraNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            return "perfis/form";
        }
        attrs.addFlashAttribute("sucesso", "Perfil salvo com sucesso");
        return "redirect:/perfis";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes attrs) {
        perfilService.excluir(id);
        attrs.addFlashAttribute("sucesso", "Perfil excluído");
        return "redirect:/perfis";
    }
}
