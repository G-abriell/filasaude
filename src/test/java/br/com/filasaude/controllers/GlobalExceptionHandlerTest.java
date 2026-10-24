package br.com.filasaude.controllers;

import br.com.filasaude.exceptions.RegraNegocioException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void redirecionaParaInicioInternoMesmoComRefererExterno() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/fila");
        request.setRequestURI("/fila/atendimentos");
        request.addHeader("Referer", "https://evil.example/");
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();

        String destino = handler.regraNegocio(new RegraNegocioException("Falha"), request, attrs);

        assertEquals("redirect:/fila/", destino);
        assertEquals("Falha", attrs.getFlashAttributes().get("erro"));
    }

    @Test
    void usaInicioInternoQuandoNaoHaContextPath() {
        HttpServletRequest request = new MockHttpServletRequest();
        ((MockHttpServletRequest) request).setContextPath("");
        ((MockHttpServletRequest) request).setRequestURI("/atendimentos");

        assertEquals("redirect:/", handler.regraNegocio(
                new RegraNegocioException("Falha"), request, new RedirectAttributesModelMap()));
    }
}
