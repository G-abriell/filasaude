package br.com.filasaude.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador responsável pela exibição da página inicial (Landing Page) do FilaSaúde.
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String exibirPaginaInicial() {
        return "index";
    }
}
