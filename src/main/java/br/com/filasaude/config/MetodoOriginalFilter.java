package br.com.filasaude.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Guarda o método HTTP original da requisição. No redirecionamento interno para /error o servidor
 * troca o método (um POST vira GET), e o registro de erros precisa saber qual ação realmente falhou.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MetodoOriginalFilter extends OncePerRequestFilter {

    private static final String ATRIBUTO = MetodoOriginalFilter.class.getName() + ".METODO";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        request.setAttribute(ATRIBUTO, request.getMethod());
        chain.doFilter(request, response);
    }

    /** Método da requisição original; se o filtro não rodou, o da requisição atual. */
    public static String de(HttpServletRequest request) {
        return request.getAttribute(ATRIBUTO) instanceof String metodo ? metodo : request.getMethod();
    }
}
