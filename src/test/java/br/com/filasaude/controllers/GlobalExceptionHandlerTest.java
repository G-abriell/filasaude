package br.com.filasaude.controllers;

import br.com.filasaude.exceptions.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private MockHttpServletRequest requisicao(String contexto, String referer) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8080);
        request.setContextPath(contexto);
        request.setRequestURI(contexto + "/atendimentos/chamar");
        if (referer != null) {
            request.addHeader("Referer", referer);
        }
        return request;
    }

    @Test
    void voltaParaAPaginaDeOrigemDoMesmoSiteMantendoAQuery() {
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();

        String destino = handler.regraNegocio(new RegraNegocioException("Falha"),
                requisicao("", "http://localhost:8080/atendimentos/guiche?filaId=2"), new ExtendedModelMap(), attrs);

        assertEquals("redirect:/atendimentos/guiche?filaId=2", destino);
        assertEquals("Falha", attrs.getFlashAttributes().get("erro"));
    }

    @Test
    void respeitaOContextPathSemDuplica() {
        String destino = handler.regraNegocio(new RegraNegocioException("Falha"),
                requisicao("/fila", "http://localhost:8080/fila/usuarios"), new ExtendedModelMap(),
                new RedirectAttributesModelMap());

        assertEquals("redirect:/usuarios", destino);
    }

    @Test
    void ignoraRefererExternoEMostraAMensagemNaPaginaDeAviso() {
        Model model = new ExtendedModelMap();
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();

        String destino = handler.regraNegocio(new RegraNegocioException("Falha"),
                requisicao("", "https://evil.example/atendimentos"), model, attrs);

        assertEquals("erro", destino);
        assertEquals("Falha", model.getAttribute("mensagem"));
        assertTrue(attrs.getFlashAttributes().isEmpty());
    }

    @Test
    void ignoraRefererDeOutraPortaOuEsquemaEProtocoloRelativo() {
        for (String referer : new String[]{
                "http://localhost:9090/atendimentos", "https://localhost:8080/atendimentos",
                "http://localhost:8080//evil.example/x", "not a uri", ""}) {
            Model model = new ExtendedModelMap();
            assertEquals("erro", handler.regraNegocio(new RegraNegocioException("Falha"),
                    requisicao("", referer), model, new RedirectAttributesModelMap()), referer);
        }
    }

    @Test
    void semRefererMostraAMensagemNaPaginaDeAviso() {
        Model model = new ExtendedModelMap();

        assertEquals("erro", handler.regraNegocio(new RegraNegocioException("Falha"),
                requisicao("", null), model, new RedirectAttributesModelMap()));
        assertEquals("Falha", model.getAttribute("mensagem"));
    }
}
