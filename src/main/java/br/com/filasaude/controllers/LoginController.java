package br.com.filasaude.controllers;

import br.com.filasaude.config.MetodoOriginalFilter;
import br.com.filasaude.services.RegistroErroService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.stream.Collectors;

@Controller
public class LoginController {

    private final RegistroErroService registroErros;

    public LoginController(RegistroErroService registroErros) {
        this.registroErros = registroErros;
    }

    @GetMapping("/login")
    public String login(Authentication authentication) {
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/inicio";
        }
        return "login";
    }

    /** Também atende POSTs recusados (ex.: token CSRF inválido), que chegam aqui por forward. */
    @RequestMapping("/acesso-negado")
    public String acessoNegado(HttpServletRequest request, Authentication authentication, Model model) {
        registrarAlertaDeSeguranca(request, authentication);
        model.addAttribute("mensagem", "Seu perfil não tem acesso a esta tela.");
        return "erro";
    }

    /**
     * Quem está logado nunca chega a uma URL proibida pelo menu, nem envia um POST com token inválido,
     * em uso normal: é tentativa direta de burlar permissão, requisição forjada ou defeito. Sem sessão,
     * um token inválido é só login expirado e não é registrado.
     */
    private void registrarAlertaDeSeguranca(HttpServletRequest request, Authentication autenticacao) {
        boolean logado = autenticacao != null && !(autenticacao instanceof AnonymousAuthenticationToken);
        if (!logado || !(request.getAttribute(WebAttributes.ACCESS_DENIED_403) instanceof AccessDeniedException negado)) {
            return;
        }
        Object uri = request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);
        String caminho = uri == null ? request.getRequestURI() : uri.toString();
        boolean csrf = negado instanceof CsrfException;
        String perfis = autenticacao.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(autoridade -> autoridade.startsWith("ROLE_"))
                .collect(Collectors.joining(","));
        String mensagem = csrf
                ? "Token CSRF inválido ou ausente em ação com sessão ativa (possível requisição forjada)."
                : "Acesso negado ao perfil " + perfis + " em uma URL fora do seu menu (acesso direto ou tentativa de burlar permissão).";
        registroErros.seguranca(mensagem, autenticacao.getName() + "|" + (csrf ? "csrf" : "negado"),
                MetodoOriginalFilter.de(request), caminho);
    }
}
