package br.com.filasaude.controllers;

import br.com.filasaude.entities.Usuario;
import br.com.filasaude.exceptions.RegraNegocioException;
import br.com.filasaude.services.PerfilService;
import br.com.filasaude.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final PerfilService perfilService;

    public UsuarioController(UsuarioService usuarioService, PerfilService perfilService) {
        this.usuarioService = usuarioService;
        this.perfilService = perfilService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listar());
        return "usuarios/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        return form(new Usuario(), model);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id);
        usuario.setSenha(null); // nunca reenviar a senha para a tela
        return form(usuario, model);
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute("usuario") Usuario usuario, BindingResult result,
                         Model model, RedirectAttributes attrs) {
        if (result.hasErrors()) {
            return form(usuario, model);
        }
        try {
            usuarioService.salvar(usuario);
        } catch (RegraNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            return form(usuario, model);
        }
        attrs.addFlashAttribute("sucesso", "Usuário salvo.");
        return "redirect:/usuarios";
    }

    @PostMapping("/{id}/alternar-ativo")
    public String alternarAtivo(@PathVariable Long id, RedirectAttributes attrs) {
        usuarioService.alternarAtivo(id);
        attrs.addFlashAttribute("sucesso", "Situação do usuário atualizada.");
        return "redirect:/usuarios";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes attrs) {
        usuarioService.excluir(id);
        attrs.addFlashAttribute("sucesso", "Usuário excluído.");
        return "redirect:/usuarios";
    }

    private String form(Usuario usuario, Model model) {
        model.addAttribute("usuario", usuario);
        model.addAttribute("perfis", perfilService.listar());
        return "usuarios/form";
    }
}
