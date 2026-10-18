package br.com.filasaude.controllers;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(Authentication authentication) {
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/";
        }
        return "login";
    }

    /** Também atende POSTs recusados (ex.: token CSRF inválido), que chegam aqui por forward. */
    @RequestMapping("/acesso-negado")
    public String acessoNegado(Model model) {
        model.addAttribute("mensagem", "Você não tem permissão para acessar esta página.");
        return "erro";
    }
}
