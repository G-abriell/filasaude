package br.com.filasaude.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador responsável pela página de configurações da unidade e parâmetros da fila.
 */
@Controller
@RequestMapping("/configuracoes")
public class ConfiguracaoController {

    private String nomeUnidade = "Unidade de Saúde Central";
    private String cnpj = "12.345.678/0001-90";
    private String tipoClinica = "Clínica Médica Geral";
    private String telefone = "(83) 9 8888-4000";
    private String endereco = "Av. Epitácio Pessoa, 1200, Centro";

    private String prefixoSenha = "A";
    private Integer tempoMedioConsulta = 15;
    private String horarioAbertura = "07:00";
    private String horarioFechamento = "17:00";

    private boolean alertaSonoro = true;
    private boolean exibirHistoricoPainel = true;

    // Preferências de Acessibilidade (IHC / WCAG 2.2)
    private String tamanhoFonte = "padrao"; // padrao, medio, grande
    private boolean altoContraste = false;
    private boolean focoReforcado = true;
    private boolean reducaoMovimento = false;
    private boolean legibilidadeAprimorada = false;


    @GetMapping
    public String exibirConfiguracoes(Model model) {
        model.addAttribute("nomeUnidade", nomeUnidade);
        model.addAttribute("cnpj", cnpj);
        model.addAttribute("tipoClinica", tipoClinica);
        model.addAttribute("telefone", telefone);
        model.addAttribute("endereco", endereco);
        model.addAttribute("prefixoSenha", prefixoSenha);
        model.addAttribute("tempoMedioConsulta", tempoMedioConsulta);
        model.addAttribute("horarioAbertura", horarioAbertura);
        model.addAttribute("horarioFechamento", horarioFechamento);
        model.addAttribute("alertaSonoro", alertaSonoro);
        model.addAttribute("exibirHistoricoPainel", exibirHistoricoPainel);

        // Acessibilidade
        model.addAttribute("tamanhoFonte", tamanhoFonte);
        model.addAttribute("altoContraste", altoContraste);
        model.addAttribute("focoReforcado", focoReforcado);
        model.addAttribute("reducaoMovimento", reducaoMovimento);
        model.addAttribute("legibilidadeAprimorada", legibilidadeAprimorada);
        return "configuracoes";
    }

    @PostMapping
    public String salvarConfiguracoes(
            @RequestParam("nomeUnidade") String nomeUnidade,
            @RequestParam(value = "cnpj", required = false, defaultValue = "") String cnpj,
            @RequestParam(value = "tipoClinica", required = false, defaultValue = "Clínica Médica Geral") String tipoClinica,
            @RequestParam(value = "telefone", required = false, defaultValue = "") String telefone,
            @RequestParam(value = "endereco", required = false, defaultValue = "") String endereco,
            @RequestParam("prefixoSenha") String prefixoSenha,
            @RequestParam("tempoMedioConsulta") Integer tempoMedioConsulta,
            @RequestParam("horarioAbertura") String horarioAbertura,
            @RequestParam("horarioFechamento") String horarioFechamento,
            @RequestParam(value = "alertaSonoro", defaultValue = "false") boolean alertaSonoro,
            @RequestParam(value = "exibirHistoricoPainel", defaultValue = "false") boolean exibirHistoricoPainel,
            @RequestParam(value = "tamanhoFonte", defaultValue = "padrao") String tamanhoFonte,
            @RequestParam(value = "altoContraste", defaultValue = "false") boolean altoContraste,
            @RequestParam(value = "focoReforcado", defaultValue = "false") boolean focoReforcado,
            @RequestParam(value = "reducaoMovimento", defaultValue = "false") boolean reducaoMovimento,
            @RequestParam(value = "legibilidadeAprimorada", defaultValue = "false") boolean legibilidadeAprimorada,
            RedirectAttributes redirectAttributes) {

        if (nomeUnidade == null || nomeUnidade.isBlank() || prefixoSenha == null || prefixoSenha.isBlank()
                || tempoMedioConsulta == null || tempoMedioConsulta < 1) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Não foi possível salvar. Confira os campos em destaque.");
            return "redirect:/configuracoes";
        }

        this.nomeUnidade = nomeUnidade.trim();
        this.cnpj = formatarCnpj(cnpj);
        this.tipoClinica = (tipoClinica == null || tipoClinica.isBlank()) ? "Clínica Médica Geral" : tipoClinica.trim();
        this.telefone = formatarTelefone(telefone);
        this.endereco = endereco.trim();
        this.prefixoSenha = prefixoSenha.trim().toUpperCase();
        this.tempoMedioConsulta = tempoMedioConsulta;
        this.horarioAbertura = horarioAbertura;
        this.horarioFechamento = horarioFechamento;
        this.alertaSonoro = alertaSonoro;
        this.exibirHistoricoPainel = exibirHistoricoPainel;

        this.tamanhoFonte = tamanhoFonte;
        this.altoContraste = altoContraste;
        this.focoReforcado = focoReforcado;
        this.reducaoMovimento = reducaoMovimento;
        this.legibilidadeAprimorada = legibilidadeAprimorada;

        redirectAttributes.addFlashAttribute("mensagemSucesso", "Configurações salvas com sucesso.");
        return "redirect:/configuracoes";
    }

    private String formatarCnpj(String val) {
        if (val == null || val.isBlank()) {
            return "";
        }
        String digitos = val.replaceAll("\\D", "");
        if (digitos.length() == 14) {
            return String.format("%s.%s.%s/%s-%s",
                    digitos.substring(0, 2), digitos.substring(2, 5), digitos.substring(5, 8),
                    digitos.substring(8, 12), digitos.substring(12, 14));
        } else if (!digitos.isEmpty()) {
            return digitos;
        }
        return "";
    }

    private String formatarTelefone(String tel) {
        if (tel == null || tel.isBlank()) {
            return "";
        }
        String digitos = tel.replaceAll("\\D", "");
        if (digitos.length() == 10) {
            return String.format("(%s) %s-%s", digitos.substring(0, 2), digitos.substring(2, 6), digitos.substring(6));
        } else if (digitos.length() == 11) {
            return String.format("(%s) %s %s-%s", digitos.substring(0, 2), digitos.substring(2, 3), digitos.substring(3, 7), digitos.substring(7));
        } else if (!digitos.isEmpty()) {
            return digitos;
        }
        return "";
    }
}
