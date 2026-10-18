package br.com.filasaude.controllers;

import br.com.filasaude.entities.Atendimento;
import br.com.filasaude.entities.enums.Prioridade;
import br.com.filasaude.security.UsuarioAutenticado;
import br.com.filasaude.services.AtendimentoService;
import br.com.filasaude.services.FilaService;
import br.com.filasaude.services.PacienteService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/atendimentos")
public class AtendimentoController {

    private final AtendimentoService atendimentoService;
    private final FilaService filaService;
    private final PacienteService pacienteService;

    public AtendimentoController(AtendimentoService atendimentoService, FilaService filaService,
                                 PacienteService pacienteService) {
        this.atendimentoService = atendimentoService;
        this.filaService = filaService;
        this.pacienteService = pacienteService;
    }

    /** Atendimentos do dia, com filtro opcional por fila. */
    @GetMapping
    public String listar(@RequestParam(required = false) Long filaId, Model model) {
        model.addAttribute("atendimentos", atendimentoService.listarDoDia(filaId));
        model.addAttribute("filas", filaService.listar());
        model.addAttribute("filaId", filaId);
        return "atendimentos/lista";
    }

    /** Tela de emissão de senha (recepção). */
    @GetMapping("/novo")
    public String novo(@RequestParam(required = false) Long pacienteId, Model model) {
        model.addAttribute("pacientes", pacienteService.listar(null));
        model.addAttribute("filas", filaService.listarAtivas());
        model.addAttribute("prioridades", Prioridade.values());
        model.addAttribute("pacienteId", pacienteId);
        return "atendimentos/form";
    }

    @PostMapping
    public String emitirSenha(@RequestParam Long pacienteId, @RequestParam Long filaId,
                              @RequestParam(required = false) Prioridade prioridade,
                              RedirectAttributes attrs) {
        Atendimento atendimento = atendimentoService.emitirSenha(pacienteId, filaId, prioridade);
        attrs.addFlashAttribute("sucesso", "Senha emitida: " + atendimento.getSenha());
        return "redirect:/atendimentos/novo";
    }

    /** Tela do atendente/guichê: fila de espera e atendimentos em andamento. */
    @GetMapping("/guiche")
    public String guiche(@RequestParam(required = false) Long filaId, Model model) {
        model.addAttribute("filas", filaService.listarAtivas());
        model.addAttribute("emAndamento", atendimentoService.listarEmAndamento());
        model.addAttribute("filaId", filaId);
        if (filaId != null) {
            model.addAttribute("aguardando", atendimentoService.listarAguardando(filaId));
        }
        return "atendimentos/guiche";
    }

    /** O atendente é o usuário autenticado. */
    @PostMapping("/chamar")
    public String chamarProximo(@RequestParam Long filaId, @RequestParam(required = false) String guiche,
                                @AuthenticationPrincipal UsuarioAutenticado usuario, RedirectAttributes attrs) {
        Atendimento atendimento = atendimentoService.chamarProximo(filaId, usuario.getId(), guiche);
        attrs.addFlashAttribute("sucesso", "Chamando " + atendimento.getSenha());
        return redirectGuiche(filaId);
    }

    @PostMapping("/{id}/iniciar")
    public String iniciar(@PathVariable Long id, @RequestParam(required = false) Long filaId) {
        atendimentoService.iniciar(id);
        return redirectGuiche(filaId);
    }

    @PostMapping("/{id}/finalizar")
    public String finalizar(@PathVariable Long id, @RequestParam(required = false) String observacao,
                            @RequestParam(required = false) Long filaId) {
        atendimentoService.finalizar(id, observacao);
        return redirectGuiche(filaId);
    }

    @PostMapping("/{id}/nao-compareceu")
    public String naoCompareceu(@PathVariable Long id, @RequestParam(required = false) Long filaId) {
        atendimentoService.registrarNaoComparecimento(id);
        return redirectGuiche(filaId);
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, RedirectAttributes attrs) {
        atendimentoService.cancelar(id);
        attrs.addFlashAttribute("sucesso", "Atendimento cancelado");
        return "redirect:/atendimentos";
    }

    private String redirectGuiche(Long filaId) {
        return "redirect:/atendimentos/guiche" + (filaId != null ? "?filaId=" + filaId : "");
    }
}
