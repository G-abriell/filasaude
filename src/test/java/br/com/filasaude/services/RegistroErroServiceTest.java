package br.com.filasaude.services;

import br.com.filasaude.entities.RegistroErro;
import br.com.filasaude.entities.enums.TipoErro;
import br.com.filasaude.repositories.RegistroErroRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RegistroErroServiceTest {

    private final RegistroErroRepository repository = mock(RegistroErroRepository.class);
    private final RegistroErroService service = new RegistroErroService(repository);

    @AfterEach
    void limpar() {
        SecurityContextHolder.clearContext();
    }

    private RegistroErro gravado() {
        ArgumentCaptor<RegistroErro> captor = ArgumentCaptor.forClass(RegistroErro.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void erroInesperadoGuardaCausaRaizPilhaUsuarioELocal() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin@x.com", "", List.of()));
        Exception erro = new RuntimeException("falha externa", new IllegalStateException("tabela ausente"));

        service.inesperado(erro, "GET", "/perfis?busca=12345678900");

        RegistroErro registro = gravado();
        assertEquals(TipoErro.INESPERADO, registro.getTipo());
        assertTrue(registro.getMensagem().contains("IllegalStateException"));
        assertTrue(registro.getMensagem().contains("tabela ausente"));
        assertTrue(registro.getDetalhe().contains("RuntimeException"));
        assertEquals("admin@x.com", registro.getUsuarioEmail());
        assertEquals(1, registro.getOcorrencias());
    }

    @Test
    void requisicaoInvalidaEAlertaDeSegurancaTemTipoProprio() {
        service.requisicaoInvalida(400, new IllegalArgumentException("filaId=abc"), "GET", "/atendimentos");
        assertEquals(TipoErro.REQUISICAO_INVALIDA, gravado().getTipo());
    }

    @Test
    void alertaDeSegurancaGuardaMensagemEChave() {
        service.seguranca("Acesso negado", "rec@x.com|negado", "POST", "/usuarios/7/excluir");

        RegistroErro registro = gravado();
        assertEquals(TipoErro.SEGURANCA, registro.getTipo());
        assertEquals("SEGURANCA|rec@x.com|negado|POST /usuarios/{id}/excluir", registro.getChave());
    }

    @Test
    void ocorrenciaIgualDentroDaJanelaSomaNaMesmaLinhaSemCriarOutra() {
        RegistroErro existente = new RegistroErro(TipoErro.INESPERADO, "chave", "msg", null, null, "GET", "/x");
        when(repository.findFirstByChaveAndUltimaOcorrenciaAfterOrderByUltimaOcorrenciaDesc(anyString(), any()))
                .thenReturn(Optional.of(existente));

        service.inesperado(new RuntimeException("x"), "GET", "/x");
        service.inesperado(new RuntimeException("x"), "GET", "/x");

        assertEquals(3, existente.getOcorrencias());
        verify(repository, times(2)).save(existente);
        verify(repository, never()).save(argThat(r -> r != existente));
    }

    @Test
    void pesquisaOGrupoSoDentroDaJanelaDeAgrupamento() {
        service.seguranca("x", "k", "GET", "/y");

        ArgumentCaptor<LocalDateTime> desde = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(repository).findFirstByChaveAndUltimaOcorrenciaAfterOrderByUltimaOcorrenciaDesc(anyString(), desde.capture());
        assertTrue(desde.getValue().isBefore(LocalDateTime.now().minusMinutes(29)));
        assertTrue(desde.getValue().isAfter(LocalDateTime.now().minusMinutes(31)));
    }

    @Test
    void normalizaIdsEQueryNaChave() {
        assertEquals("/pacientes/{id}/excluir", RegistroErroService.normalizar("/pacientes/12/excluir"));
        assertEquals("/pacientes/{id}", RegistroErroService.normalizar("/pacientes/12?busca=123"));
        assertEquals("/atendimentos/guiche", RegistroErroService.normalizar("/atendimentos/guiche"));
        assertEquals("", RegistroErroService.normalizar(null));
    }

    @Test
    void cortaTextosLongosNosLimitesDasColunas() {
        service.inesperado(new RuntimeException("x".repeat(2000)), "GET", "/a");

        assertEquals(500, gravado().getMensagem().length());
    }

    @Test
    void falhaAoGravarNuncaCausaOutroErro() {
        when(repository.save(any())).thenThrow(new IllegalStateException("banco fora do ar"));

        assertDoesNotThrow(() -> service.inesperado(new RuntimeException("x"), "GET", "/"));
        assertDoesNotThrow(() -> service.seguranca("x", "k", "GET", "/"));
    }

    @Test
    void semUsuarioLogadoGravaUsuarioNulo() {
        service.seguranca("Varredura", "login-endereco|1.2.3.4", "POST", "/login");

        assertNull(gravado().getUsuarioEmail());
    }
}
