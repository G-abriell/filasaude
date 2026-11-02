package br.com.filasaude.controllers;

import br.com.filasaude.entities.enums.TipoErro;
import br.com.filasaude.services.RegistroErroService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Consulta dos erros ocorridos no sistema. Acesso restrito ao administrador (SecurityConfig). */
@Controller
@RequestMapping("/erros")
public class RegistroErroController {

    private final RegistroErroService registroErros;

    public RegistroErroController(RegistroErroService registroErros) {
        this.registroErros = registroErros;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) TipoErro tipo,
                         @RequestParam(defaultValue = "0") int pagina, Model model) {
        model.addAttribute("pagina", registroErros.listar(tipo, pagina));
        model.addAttribute("tipos", TipoErro.values());
        model.addAttribute("tipo", tipo);
        model.addAttribute("ultimas24h", registroErros.contarUltimas24Horas());
        return "erros/lista";
    }
}
