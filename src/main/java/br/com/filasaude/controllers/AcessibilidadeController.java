package br.com.filasaude.controllers;

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

    private String tamanhoFonte = "padrao";
    private boolean altoContraste;
    private boolean focoReforcado = true;
    private boolean reducaoMovimento;
    private boolean legibilidadeAprimorada;

    @GetMapping
    public String exibir(Model model) {
        model.addAttribute("tamanhoFonte", tamanhoFonte);
        model.addAttribute("altoContraste", altoContraste);
        model.addAttribute("focoReforcado", focoReforcado);
        model.addAttribute("reducaoMovimento", reducaoMovimento);
        model.addAttribute("legibilidadeAprimorada", legibilidadeAprimorada);
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
        this.tamanhoFonte = tamanhoFonte;
        this.altoContraste = altoContraste;
        this.focoReforcado = focoReforcado;
        this.reducaoMovimento = reducaoMovimento;
        this.legibilidadeAprimorada = legibilidadeAprimorada;
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Preferências de acessibilidade salvas.");
        return "redirect:/acessibilidade";
    }
}
