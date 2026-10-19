package br.com.filasaude.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador responsável pela página inicial (Landing Page) e pelo início do sistema após o login.
 */
@Controller
public class HomeController {

    /** Landing page pública, exibida antes do login. */
    @GetMapping("/")
    public String exibirPaginaInicial() {
        return "index";
    }

    /** Início do sistema conforme o perfil: painel de atendimento para profissionais de saúde, emissão de senha para a recepção. */
    @GetMapping("/inicio")
    public String inicio(Authentication authentication) {
        if (temPerfil(authentication, "ROLE_PROFISSIONAL_DE_SAUDE") || temPerfil(authentication, "ROLE_ADMIN")) {
            return "redirect:/atendimentos/guiche";
        }
        if (temPerfil(authentication, "ROLE_RECEPCAO")) {
            return "redirect:/atendimentos/novo";
        }
        return "redirect:/atendimentos";
    }

    private boolean temPerfil(Authentication authentication, String authority) {
        return authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(authority));
    }
}
