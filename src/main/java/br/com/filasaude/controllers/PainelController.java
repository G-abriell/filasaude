package br.com.filasaude.controllers;

import br.com.filasaude.entities.Atendimento;
import br.com.filasaude.services.AtendimentoService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Controlador do Painel de Chamada para TV/Monitor da sala de espera.
 * Acessível a qualquer usuário autenticado da unidade.
 */
@Controller
@RequestMapping("/painel")
public class PainelController {

    private final AtendimentoService atendimentoService;
    public PainelController(AtendimentoService atendimentoService) {
        this.atendimentoService = atendimentoService;
    }

    @GetMapping
    public String exibirPainel(Model model) {
        List<Atendimento> chamados = atendimentoService.listarChamadosDoDia();
        Atendimento atual = chamados.isEmpty() ? null : chamados.get(0);
        List<Atendimento> historico = chamados.size() > 1
                ? chamados.subList(1, Math.min(chamados.size(), 6))
                : Collections.emptyList();

        model.addAttribute("atual", atual);
        model.addAttribute("historico", historico);
        return "painel";
    }

    @GetMapping("/dados")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> dadosTempoReal() {
        List<Atendimento> chamados = atendimentoService.listarChamadosDoDia();
        Map<String, Object> resposta = new LinkedHashMap<>();

        if (!chamados.isEmpty()) {
            Atendimento atual = chamados.get(0);
            resposta.put("atual", mapearAtendimento(atual));

            List<Map<String, Object>> historico = new ArrayList<>();
            for (int i = 1; i < Math.min(chamados.size(), 6); i++) {
                historico.add(mapearAtendimento(chamados.get(i)));
            }
            resposta.put("historico", historico);
        } else {
            resposta.put("atual", null);
            resposta.put("historico", Collections.emptyList());
        }

        resposta.put("horaAtual", LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.EXPIRES, "0")
                .body(resposta);
    }

    private Map<String, Object> mapearAtendimento(Atendimento a) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", a.getId());
        map.put("senha", a.getSenha());
        map.put("paciente", a.getPaciente().getNome());
        map.put("fila", a.getFila().getNome());
        map.put("prioridade", a.getPrioridade().name());
        map.put("prioridadeDescricao", a.getPrioridade().getDescricao());
        map.put("guiche", a.getGuiche() != null ? a.getGuiche() : "Consultório Geral");
        map.put("horario", a.getDataHoraChamada() != null
                ? a.getDataHoraChamada().format(DateTimeFormatter.ofPattern("HH:mm"))
                : "");
        map.put("chamadaTimestamp", a.getDataHoraChamada() != null
                ? a.getDataHoraChamada().toString()
                : "");
        return map;
    }
}
