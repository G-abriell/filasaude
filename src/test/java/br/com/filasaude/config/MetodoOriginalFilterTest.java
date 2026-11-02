package br.com.filasaude.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MetodoOriginalFilterTest {

    @Test
    void guardaOMetodoOriginalMesmoQuandoARequisicaoViraGetNoRedirecionamentoDeErro() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/atendimentos");
        FilterChain cadeia = mock(FilterChain.class);

        new MetodoOriginalFilter().doFilter(request, new MockHttpServletResponse(), cadeia);
        request.setMethod("GET");   // o que o servidor faz ao encaminhar para /error

        assertEquals("POST", MetodoOriginalFilter.de(request));
        verify(cadeia).doFilter(eq(request), any());
    }

    @Test
    void semOFiltroUsaOMetodoAtual() {
        assertEquals("DELETE", MetodoOriginalFilter.de(new MockHttpServletRequest("DELETE", "/x")));
    }
}
