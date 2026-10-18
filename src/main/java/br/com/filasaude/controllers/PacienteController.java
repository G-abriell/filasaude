package br.com.filasaude.controllers;

import br.com.filasaude.entities.Paciente;
import br.com.filasaude.exceptions.RegraNegocioException;
import br.com.filasaude.services.AtendimentoService;
import br.com.filasaude.services.PacienteService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;
    private final AtendimentoService atendimentoService;

    public PacienteController(PacienteService pacienteService, AtendimentoService atendimentoService) {
        this.pacienteService = pacienteService;
        this.atendimentoService = atendimentoService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String busca, Model model) {
        model.addAttribute("pacientes", pacienteService.listar(busca));
        model.addAttribute("busca", busca);
        return "pacientes/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("paciente", new Paciente());
        return "pacientes/form";
    }

    @GetMapping("/{id}")
    public String detalhes(@PathVariable Long id, Model model) {
        model.addAttribute("paciente", pacienteService.buscarPorId(id));
        model.addAttribute("atendimentos", atendimentoService.historicoDoPaciente(id));
        return "pacientes/detalhes";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("paciente", pacienteService.buscarPorId(id));
        return "pacientes/form";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute("paciente") Paciente paciente, BindingResult result,
                         Model model, RedirectAttributes attrs) {
        if (result.hasErrors()) {
            return "pacientes/form";
        }
        try {
            paciente = pacienteService.salvar(paciente);
        } catch (RegraNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            return "pacientes/form";
        }
        attrs.addFlashAttribute("sucesso", "Paciente salvo.");
        return "redirect:/pacientes/" + paciente.getId();
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes attrs) {
        pacienteService.excluir(id);
        attrs.addFlashAttribute("sucesso", "Paciente excluído.");
        return "redirect:/pacientes";
    }
}
