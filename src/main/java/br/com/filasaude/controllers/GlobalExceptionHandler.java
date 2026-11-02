package br.com.filasaude.controllers;

import br.com.filasaude.exceptions.RecursoNaoEncontradoException;
import br.com.filasaude.exceptions.RegraNegocioException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;
import java.net.URISyntaxException;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Regra de negócio violada em uma ação (POST): volta para a página de origem exibindo a mensagem.
     * O Referer só é aceito se for do mesmo site; sem uma origem confiável, a mensagem é mostrada
     * na página de aviso (nunca se redireciona para endereços externos).
     */
    @ExceptionHandler(RegraNegocioException.class)
    public String regraNegocio(RegraNegocioException e, HttpServletRequest request,
                               Model model, RedirectAttributes attrs) {
        String origem = caminhoDeOrigem(request);
        if (origem == null) {
            model.addAttribute("mensagem", e.getMessage());
            return "erro";
        }
        attrs.addFlashAttribute("erro", e.getMessage());
        return "redirect:" + origem;
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String naoEncontrado(RecursoNaoEncontradoException e, Model model) {
        model.addAttribute("mensagem", e.getMessage());
        return "erro";
    }

    /** Caminho (relativo ao contexto da aplicação, com query) do Referer do mesmo site, ou null. */
    private static String caminhoDeOrigem(HttpServletRequest request) {
        String referer = request.getHeader(HttpHeaders.REFERER);
        if (referer == null || referer.isBlank()) {
            return null;
        }
        try {
            URI uri = new URI(referer.trim());
            if (!mesmoSite(uri, request)) {
                return null;
            }
            String contexto = request.getContextPath() == null ? "" : request.getContextPath();
            String caminho = uri.getRawPath();
            if (caminho == null || !caminho.startsWith(contexto + "/")) {
                return null;
            }
            caminho = caminho.substring(contexto.length());
            if (caminho.startsWith("//")) {
                return null;
            }
            return uri.getRawQuery() == null ? caminho : caminho + "?" + uri.getRawQuery();
        } catch (URISyntaxException ex) {
            return null;
        }
    }

    private static boolean mesmoSite(URI uri, HttpServletRequest request) {
        if (uri.getHost() == null || uri.getScheme() == null
                || !uri.getScheme().equalsIgnoreCase(request.getScheme())
                || !uri.getHost().equalsIgnoreCase(request.getServerName())) {
            return false;
        }
        int porta = uri.getPort() != -1 ? uri.getPort() : ("https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80);
        return porta == request.getServerPort();
    }
}
