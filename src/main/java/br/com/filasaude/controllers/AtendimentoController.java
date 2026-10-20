package br.com.filasaude.controllers;

import br.com.filasaude.entities.Atendimento;
import br.com.filasaude.entities.enums.Prioridade;
import br.com.filasaude.security.UsuarioAutenticado;
import br.com.filasaude.services.AtendimentoService;
import br.com.filasaude.services.FilaService;
import br.com.filasaude.services.PacienteService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/atendimentos")
public class AtendimentoController {

    private static final String SESSION_GUICHE_KEY = "guicheAtendimento";

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

    /** Visualização detalhada do registro clínico, prescrição e orientações do atendimento. */
    @GetMapping("/{id}/resumo")
    public String resumo(@PathVariable Long id, Model model) {
        model.addAttribute("atendimento", atendimentoService.buscarDetalhadoPorId(id));
        return "atendimentos/resumo";
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
        attrs.addFlashAttribute("sucesso",
                "Paciente adicionado à fila com sucesso. Senha gerada: " + atendimento.getSenha() + ".");
        // Dados do cartão de senha exibido na recepção
        attrs.addFlashAttribute("senhaGerada", atendimento.getSenha());
        attrs.addFlashAttribute("senhaPaciente", atendimento.getPaciente().getNome());
        attrs.addFlashAttribute("senhaFila", atendimento.getFila().getNome());
        attrs.addFlashAttribute("senhaPrioridade", atendimento.getPrioridade().getDescricao());
        return "redirect:/atendimentos/novo";
    }

    /** Painel do profissional de saúde: fila unificada ou filtrada e atendimentos em andamento. */
    @GetMapping("/guiche")
    public String guiche(@RequestParam(required = false) Long filaId, HttpSession session, Model model) {
        List<Atendimento> todosAguardando = atendimentoService.listarAguardando(null);
        Map<Long, Long> contagemPorFila = todosAguardando.stream()
                .collect(Collectors.groupingBy(a -> a.getFila().getId(), Collectors.counting()));

        List<Atendimento> aguardando = (filaId == null)
                ? todosAguardando
                : todosAguardando.stream()
                        .filter(a -> a.getFila().getId().equals(filaId))
                        .toList();

        model.addAttribute("filas", filaService.listarAtivas());
        model.addAttribute("emAndamento", atendimentoService.listarEmAndamento());
        model.addAttribute("filaId", filaId);
        model.addAttribute("aguardando", aguardando);
        model.addAttribute("totalAguardando", todosAguardando.size());
        model.addAttribute("contagemPorFila", contagemPorFila);
        model.addAttribute("guicheAtual", session.getAttribute(SESSION_GUICHE_KEY));
        return "atendimentos/guiche";
    }

    /** Chama o próximo paciente (da fila selecionada ou da fila geral unificada). */
    @PostMapping("/chamar")
    public String chamarProximo(@RequestParam(required = false) Long filaId,
                                @RequestParam(required = false) String guiche,
                                HttpSession session,
                                @AuthenticationPrincipal UsuarioAutenticado usuario,
                                RedirectAttributes attrs) {
        salvarGuicheNaSessao(session, guiche);
        Atendimento atendimento = atendimentoService.chamarProximo(filaId, usuario.getId(), guiche);
        attrs.addFlashAttribute("sucesso",
                "Senha " + atendimento.getSenha() + " (" + atendimento.getPaciente().getNome() + ") chamada.");
        return redirectGuiche(filaId);
    }

    /** Chama diretamente um paciente específico da lista de espera. */
    @PostMapping("/{id}/chamar")
    public String chamarPorId(@PathVariable Long id,
                              @RequestParam(required = false) Long filaId,
                              @RequestParam(required = false) String guiche,
                              HttpSession session,
                              @AuthenticationPrincipal UsuarioAutenticado usuario,
                              RedirectAttributes attrs) {
        salvarGuicheNaSessao(session, guiche);
        Atendimento atendimento = atendimentoService.chamarPorId(id, usuario.getId(), guiche);
        attrs.addFlashAttribute("sucesso",
                "Senha " + atendimento.getSenha() + " (" + atendimento.getPaciente().getNome() + ") chamada.");
        return redirectGuiche(filaId);
    }

    /** Rechama no painel um paciente que já está com status CHAMADO. */
    @PostMapping("/{id}/rechamar")
    public String rechamar(@PathVariable Long id,
                           @RequestParam(required = false) Long filaId,
                           @RequestParam(required = false) String guiche,
                           HttpSession session,
                           @AuthenticationPrincipal UsuarioAutenticado usuario,
                           RedirectAttributes attrs) {
        salvarGuicheNaSessao(session, guiche);
        Atendimento atendimento = atendimentoService.rechamar(id, usuario.getId(), guiche);
        attrs.addFlashAttribute("sucesso",
                "Senha " + atendimento.getSenha() + " chamada novamente no painel.");
        return redirectGuiche(filaId);
    }

    @PostMapping("/{id}/iniciar")
    public String iniciar(@PathVariable Long id, @RequestParam(required = false) Long filaId,
                          RedirectAttributes attrs) {
        atendimentoService.iniciar(id);
        attrs.addFlashAttribute("sucesso", "Atendimento iniciado.");
        return redirectGuiche(filaId);
    }

    @PostMapping("/{id}/finalizar")
    public String finalizar(@PathVariable Long id,
                            @RequestParam(required = false) String observacao,
                            @RequestParam(required = false) String prescricao,
                            @RequestParam(required = false) String encaminhamento,
                            @RequestParam(required = false) Long filaId,
                            RedirectAttributes attrs) {
        atendimentoService.finalizar(id, observacao, prescricao, encaminhamento);
        attrs.addFlashAttribute("sucesso",
                "Atendimento finalizado e informações enviadas para a recepção.");
        return redirectGuiche(filaId);
    }

    @PostMapping("/{id}/nao-compareceu")
    public String naoCompareceu(@PathVariable Long id, @RequestParam(required = false) Long filaId,
                                RedirectAttributes attrs) {
        atendimentoService.registrarNaoComparecimento(id);
        attrs.addFlashAttribute("sucesso", "Registrado como não compareceu.");
        return redirectGuiche(filaId);
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, RedirectAttributes attrs) {
        atendimentoService.cancelar(id);
        attrs.addFlashAttribute("sucesso", "Atendimento cancelado.");
        return "redirect:/atendimentos";
    }

    private void salvarGuicheNaSessao(HttpSession session, String guiche) {
        if (guiche != null && !guiche.isBlank()) {
            session.setAttribute(SESSION_GUICHE_KEY, guiche.trim());
        }
    }

    private String redirectGuiche(Long filaId) {
        return "redirect:/atendimentos/guiche" + (filaId != null ? "?filaId=" + filaId : "");
    }
}
