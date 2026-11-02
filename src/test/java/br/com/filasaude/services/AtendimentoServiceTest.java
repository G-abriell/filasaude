package br.com.filasaude.services;

import br.com.filasaude.entities.Atendimento;
import br.com.filasaude.entities.Fila;
import br.com.filasaude.entities.Paciente;
import br.com.filasaude.entities.Perfil;
import br.com.filasaude.entities.Usuario;
import br.com.filasaude.entities.enums.Prioridade;
import br.com.filasaude.entities.enums.StatusAtendimento;
import br.com.filasaude.exceptions.RecursoNaoEncontradoException;
import br.com.filasaude.exceptions.RegraNegocioException;
import br.com.filasaude.repositories.AtendimentoRepository;
import br.com.filasaude.repositories.FilaRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AtendimentoServiceTest {

    private final AtendimentoRepository atendimentos = mock(AtendimentoRepository.class);
    private final FilaRepository filas = mock(FilaRepository.class);
    private final PacienteService pacientes = mock(PacienteService.class);
    private final UsuarioService usuarios = mock(UsuarioService.class);
    private final AtendimentoService service = new AtendimentoService(atendimentos, filas, pacientes, usuarios);

    private final Fila fila = filaComSigla("C");
    private final LocalDateTime base = LocalDateTime.now().minusMinutes(60);

    private static Fila filaComSigla(String sigla) {
        Fila fila = new Fila();
        fila.setNome("Consulta Médica");
        fila.setSigla(sigla);
        return fila;
    }

    private Atendimento atendimento(String nome, Prioridade prioridade, int numero, int minutosAposBase) {
        Paciente paciente = new Paciente();
        paciente.setNome(nome);
        return new Atendimento(paciente, fila, prioridade, numero, base.plusMinutes(minutosAposBase));
    }

    private Usuario atendenteAtivo() {
        Usuario atendente = new Usuario();
        atendente.setNome("Dra. Ana");
        atendente.setAtivo(true);
        when(usuarios.buscarPorId(9L)).thenReturn(atendente);
        return atendente;
    }

    @Test
    void deveIntercalarDoisPreferenciaisParaUmNormalNaFila() {
        Atendimento pref1 = atendimento("Idoso 1", Prioridade.SUPERPREFERENCIAL, 2, 2);
        Atendimento pref2 = atendimento("Idoso 2", Prioridade.PREFERENCIAL, 3, 4);
        Atendimento pref3 = atendimento("Gestante 3", Prioridade.GESTANTE, 4, 6);
        Atendimento pref4 = atendimento("PCD 4", Prioridade.PCD, 5, 8);
        Atendimento norm1 = atendimento("João Normal 1", Prioridade.NORMAL, 1, 0);
        Atendimento norm2 = atendimento("Maria Normal 2", Prioridade.NORMAL, 6, 10);

        // 0 preferenciais chamados antes: [Pref1, Pref2, Norm1, Pref3, Pref4, Norm2]
        assertEquals(List.of(pref1, pref2, norm1, pref3, pref4, norm2),
                AtendimentoService.intercalarFila2Por1(List.of(pref1, pref2, pref3, pref4, norm1, norm2), 0));

        // 1 preferencial já chamado antes: falta 1 preferencial e depois 1 normal
        assertEquals(List.of(pref2, norm1, pref3, pref4, norm2),
                AtendimentoService.intercalarFila2Por1(List.of(pref2, pref3, pref4, norm1, norm2), 1));

        // 2 preferenciais já chamados seguidos: o próximo DEVE ser o normal
        assertEquals(List.of(norm1, pref3, pref4, norm2),
                AtendimentoService.intercalarFila2Por1(List.of(pref3, pref4, norm1, norm2), 2));
    }

    @Test
    void listaAguardandoOrdenaPorPrioridadeEDepoisPorChegada() {
        Atendimento normalAntigo = atendimento("Normal antigo", Prioridade.NORMAL, 1, 0);
        Atendimento preferencialTarde = atendimento("Preferencial tarde", Prioridade.GESTANTE, 3, 20);
        Atendimento preferencialCedo = atendimento("Preferencial cedo", Prioridade.PCD, 2, 10);
        Atendimento superPrioridade = atendimento("Superpreferencial", Prioridade.SUPERPREFERENCIAL, 4, 30);
        // o repositório devolve por ordem de chegada
        when(atendimentos.findAguardandoNaFila(1L))
                .thenReturn(List.of(normalAntigo, preferencialCedo, preferencialTarde, superPrioridade));
        when(atendimentos.findUltimosChamadosDoDia(any(), any())).thenReturn(List.of());

        // Sem preferenciais chamados antes: Super, Pref cedo, Normal (intercalado) e Pref tarde
        assertEquals(List.of(superPrioridade, preferencialCedo, normalAntigo, preferencialTarde),
                service.listarAguardando(1L));
    }

    @Test
    void chamarProximoSemFilaTravaTodasAsFilasAtivasAntesDeLerOsAguardando() {
        Usuario atendente = atendenteAtivo();
        Atendimento normal = atendimento("Normal", Prioridade.NORMAL, 1, 0);
        Atendimento superPrioridade = atendimento("Super", Prioridade.SUPERPREFERENCIAL, 2, 5);
        when(filas.findAtivasComLock()).thenReturn(List.of(fila));
        when(atendimentos.findAllAguardando()).thenReturn(List.of(normal, superPrioridade));
        when(atendimentos.findUltimosChamadosDoDia(any(), any())).thenReturn(List.of());

        Atendimento chamado = service.chamarProximo(null, 9L, "Sala 1");

        assertSame(superPrioridade, chamado);
        assertEquals(StatusAtendimento.CHAMADO, chamado.getStatus());
        assertSame(atendente, chamado.getAtendente());
        InOrder ordem = inOrder(filas, atendimentos);
        ordem.verify(filas).findAtivasComLock();
        ordem.verify(atendimentos).findAllAguardando();
    }

    @Test
    void chamarProximoDeFilaEspecificaTravaAFilaAntesDeLer() {
        atendenteAtivo();
        Atendimento normal = atendimento("Normal", Prioridade.NORMAL, 1, 0);
        when(filas.findByIdComLock(1L)).thenReturn(Optional.of(fila));
        when(atendimentos.findAguardandoNaFila(1L)).thenReturn(List.of(normal));
        when(atendimentos.findUltimosChamadosDoDia(any(), any())).thenReturn(List.of());

        assertSame(normal, service.chamarProximo(1L, 9L, null));

        InOrder ordem = inOrder(filas, atendimentos);
        ordem.verify(filas).findByIdComLock(1L);
        ordem.verify(atendimentos).findAguardandoNaFila(1L);
    }

    @Test
    void chamarProximoRecusaFilaInativaEFilaVazia() {
        atendenteAtivo();
        Fila inativa = filaComSigla("I");
        inativa.setAtiva(false);
        when(filas.findByIdComLock(2L)).thenReturn(Optional.of(inativa));
        assertThrows(RegraNegocioException.class, () -> service.chamarProximo(2L, 9L, null));

        when(filas.findAtivasComLock()).thenReturn(List.of(fila));
        when(atendimentos.findAllAguardando()).thenReturn(List.of());
        when(atendimentos.findUltimosChamadosDoDia(any(), any())).thenReturn(List.of());
        assertThrows(RegraNegocioException.class, () -> service.chamarProximo(null, 9L, null));
    }

    @Test
    void chamarPorIdTravaAFilaAntesDeCarregarOAtendimento() {
        atendenteAtivo();
        Atendimento aguardando = atendimento("Paciente", Prioridade.NORMAL, 1, 0);
        when(atendimentos.findFilaIdById(5L)).thenReturn(Optional.of(3L));
        when(filas.findByIdComLock(3L)).thenReturn(Optional.of(fila));
        when(atendimentos.findByIdDetalhado(5L)).thenReturn(Optional.of(aguardando));

        Atendimento chamado = service.chamarPorId(5L, 9L, "Sala 2");

        assertEquals(StatusAtendimento.CHAMADO, chamado.getStatus());
        InOrder ordem = inOrder(filas, atendimentos);
        ordem.verify(atendimentos).findFilaIdById(5L);
        ordem.verify(filas).findByIdComLock(3L);
        ordem.verify(atendimentos).findByIdDetalhado(5L);
    }

    private Usuario usuarioComPerfil(Long id, String nome, String perfil, boolean ativo) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setAtivo(ativo);
        usuario.setPerfil(new Perfil(1L, perfil, ""));
        when(usuarios.buscarPorId(id)).thenReturn(usuario);
        return usuario;
    }

    @Test
    void chamadaEmContingenciaAtribuiAoProfissionalERegistraQuemExecutou() {
        Usuario recepcao = usuarioComPerfil(5L, "Rita", "RECEPCAO", true);
        Usuario profissional = usuarioComPerfil(7L, "Dr. Paulo", "PROFISSIONAL_DE_SAUDE", true);
        Atendimento normal = atendimento("Normal", Prioridade.NORMAL, 1, 0);
        Atendimento superPrioridade = atendimento("Super", Prioridade.SUPERPREFERENCIAL, 2, 5);
        when(filas.findAtivasComLock()).thenReturn(List.of(fila));
        when(atendimentos.findAllAguardando()).thenReturn(List.of(normal, superPrioridade));
        when(atendimentos.findUltimosChamadosDoDia(any(), any())).thenReturn(List.of());

        Atendimento chamado = service.chamarProximoEmNomeDe(null, 5L, 7L, "Sala 4");

        assertSame(superPrioridade, chamado);                 // mesma ordem de prioridade
        assertSame(profissional, chamado.getAtendente());     // quem aparece e assina
        assertSame(recepcao, chamado.getChamadoPor());        // quem de fato chamou
        assertEquals(StatusAtendimento.CHAMADO, chamado.getStatus());
    }

    @Test
    void chamadaEmContingenciaRecusaQuemNaoEProfissionalDeSaudeOuEstaInativo() {
        usuarioComPerfil(5L, "Rita", "RECEPCAO", true);
        usuarioComPerfil(6L, "Outra recepcionista", "RECEPCAO", true);
        usuarioComPerfil(8L, "Dr. Afastado", "PROFISSIONAL_DE_SAUDE", false);

        assertThrows(RegraNegocioException.class, () -> service.chamarProximoEmNomeDe(null, 5L, 6L, null));
        assertThrows(RegraNegocioException.class, () -> service.chamarProximoEmNomeDe(null, 5L, 8L, null));
        verify(atendimentos, never()).findAllAguardando();
    }

    @Test
    void chamarPorIdInexistenteLancaRecursoNaoEncontrado() {
        atendenteAtivo();
        when(atendimentos.findFilaIdById(404L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.chamarPorId(404L, 9L, null));
        verify(filas, never()).findByIdComLock(any());
    }
}
