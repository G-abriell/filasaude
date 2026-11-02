package br.com.filasaude.controllers;

import br.com.filasaude.config.MetodoOriginalFilter;
import br.com.filasaude.services.RegistroErroService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorAttributes;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.Set;

/**
 * Página de erro da aplicação (substitui a página padrão do Spring). Só vão para o administrador os
 * problemas que o usuário não resolve sozinho: falhas do servidor (5xx) e requisições que a interface
 * nunca gera (400, 405, 415: parâmetro adulterado, método errado). 404 e 403 apenas mostram o aviso.
 */
@Controller
public class PaginaErroController implements ErrorController {

    private static final Set<Integer> REQUISICOES_INVALIDAS = Set.of(400, 405, 415);

    private final ErrorAttributes errorAttributes;
    private final RegistroErroService registroErros;

    public PaginaErroController(ErrorAttributes errorAttributes, RegistroErroService registroErros) {
        this.errorAttributes = errorAttributes;
        this.registroErros = registroErros;
    }

    @RequestMapping("/error")
    public String erro(HttpServletRequest request, Model model) {
        Object statusAtributo = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = statusAtributo instanceof Integer codigo ? codigo : 500;
        Object caminhoAtributo = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        String caminho = caminhoAtributo == null ? null : caminhoAtributo.toString();
        Throwable causa = errorAttributes.getError(new ServletWebRequest(request));
        String metodo = MetodoOriginalFilter.de(request);

        if (status >= 500) {
            registroErros.inesperado(causa, metodo, caminho);
            model.addAttribute("mensagem", "Ocorreu um erro inesperado. O administrador foi avisado.");
        } else if (REQUISICOES_INVALIDAS.contains(status)) {
            registroErros.requisicaoInvalida(status, causa, metodo, caminho);
            model.addAttribute("mensagem", "Não foi possível concluir a solicitação.");
        } else if (status == 404) {
            model.addAttribute("mensagem", "Página não encontrada.");
        } else if (status == 403) {
            model.addAttribute("mensagem", "Seu perfil não tem acesso a esta tela.");
        } else {
            model.addAttribute("mensagem", "Não foi possível concluir a solicitação.");
        }
        return "erro";
    }
}
