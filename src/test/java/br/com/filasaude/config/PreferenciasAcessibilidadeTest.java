package br.com.filasaude.config;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PreferenciasAcessibilidadeTest {

    private final PreferenciasAcessibilidade preferencias = new PreferenciasAcessibilidade();

    @Test
    void comecaComOsValoresPadrao() {
        Model model = new ExtendedModelMap();
        preferencias.adicionarAo(model);

        assertEquals("padrao", model.getAttribute("tamanhoFonte"));
        assertEquals(false, model.getAttribute("altoContraste"));
        assertEquals(true, model.getAttribute("focoReforcado"));
        assertEquals(false, model.getAttribute("reducaoMovimento"));
        assertEquals(false, model.getAttribute("legibilidadeAprimorada"));
    }

    @Test
    void atualizacaoFicaVisivelParaQuemConsultarDepoisEIgnoraTamanhoInvalido() {
        preferencias.atualizar("grande", true, false, true, true);
        Model model = new ExtendedModelMap();
        preferencias.adicionarAo(model);

        assertEquals("grande", model.getAttribute("tamanhoFonte"));
        assertEquals(true, model.getAttribute("altoContraste"));
        assertEquals(false, model.getAttribute("focoReforcado"));

        preferencias.atualizar("<script>", false, true, false, false);
        Model outro = new ExtendedModelMap();
        preferencias.adicionarAo(outro);
        assertEquals("padrao", outro.getAttribute("tamanhoFonte"));
    }
}
