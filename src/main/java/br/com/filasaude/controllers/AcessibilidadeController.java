package br.com.filasaude.controllers;

import br.com.filasaude.config.PreferenciasAcessibilidade;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/acessibilidade")
public class AcessibilidadeController {

    private final PreferenciasAcessibilidade preferencias;

    public AcessibilidadeController(PreferenciasAcessibilidade preferencias) {
        this.preferencias = preferencias;
    }

    @GetMapping
    public String exibir(Model model) {
        preferencias.adicionarAo(model);
        return "acessibilidade";
    }

    @PostMapping
    public String salvar(
            @RequestParam(value = "tamanhoFonte", defaultValue = "padrao") String tamanhoFonte,
            @RequestParam(value = "altoContraste", defaultValue = "false") boolean altoContraste,
            @RequestParam(value = "focoReforcado", defaultValue = "false") boolean focoReforcado,
            @RequestParam(value = "reducaoMovimento", defaultValue = "false") boolean reducaoMovimento,
            @RequestParam(value = "legibilidadeAprimorada", defaultValue = "false") boolean legibilidadeAprimorada,
            RedirectAttributes redirectAttributes) {
        preferencias.atualizar(tamanhoFonte, altoContraste, focoReforcado, reducaoMovimento, legibilidadeAprimorada);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Preferências de acessibilidade salvas.");
        return "redirect:/acessibilidade";
    }
}
