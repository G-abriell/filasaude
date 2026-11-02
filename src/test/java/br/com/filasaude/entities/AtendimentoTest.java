package br.com.filasaude.entities;

import br.com.filasaude.entities.enums.Prioridade;
import br.com.filasaude.entities.enums.StatusAtendimento;
import br.com.filasaude.exceptions.RegraNegocioException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AtendimentoTest {

    private Atendimento novoAtendimento() {
        Paciente paciente = new Paciente();
        paciente.setNome("Carlos Souza");
        Fila fila = new Fila();
        fila.setNome("Consulta Médica");
        fila.setSigla("C");
        return new Atendimento(paciente, fila, Prioridade.NORMAL, 1, LocalDateTime.now().minusMinutes(15));
    }

    @Test
    void deveRegistrarEvolucaoPrescricaoEEncaminhamentoParaRecepcao() {
        Atendimento atendimento = novoAtendimento();
        Usuario medico = new Usuario();
        medico.setNome("Dr. Roberto");

        assertEquals("C001", atendimento.getSenha());
        assertEquals(StatusAtendimento.AGUARDANDO, atendimento.getStatus());

        atendimento.chamar(medico, "Consultório 1");
        assertEquals(StatusAtendimento.CHAMADO, atendimento.getStatus());
        assertEquals("Consultório 1", atendimento.getGuiche());

        atendimento.rechamar(medico, "Consultório 2");
        assertEquals(StatusAtendimento.CHAMADO, atendimento.getStatus());
        assertEquals("Consultório 2", atendimento.getGuiche());

        atendimento.iniciar();
        assertEquals(StatusAtendimento.EM_ATENDIMENTO, atendimento.getStatus());

        atendimento.finalizar(
                "Paciente com quadro febril há 2 dias, hidratado.",
                "Dipirona 500mg - 1 cp de 6/6h se febre.",
                "Receita disponível para impressão · Agendar retorno em 7 dias");

        assertEquals(StatusAtendimento.FINALIZADO, atendimento.getStatus());
        assertTrue(atendimento.possuiRegistroClinico());
        assertEquals("Dipirona 500mg - 1 cp de 6/6h se febre.", atendimento.getPrescricao());
        assertEquals("Receita disponível para impressão · Agendar retorno em 7 dias", atendimento.getEncaminhamento());
    }

    @Test
    void textosEmBrancoViramNuloEGuicheEmBrancoNaoSobrescreveORechamar() {
        Atendimento atendimento = novoAtendimento();
        Usuario medico = new Usuario();

        atendimento.chamar(medico, "  Sala 3  ");
        assertEquals("Sala 3", atendimento.getGuiche());

        atendimento.rechamar(medico, "   ");
        assertEquals("Sala 3", atendimento.getGuiche());

        atendimento.iniciar();
        atendimento.finalizar("  ", null, "");
        assertNull(atendimento.getObservacao());
        assertNull(atendimento.getPrescricao());
        assertNull(atendimento.getEncaminhamento());
        assertFalse(atendimento.possuiRegistroClinico());
    }

    @Test
    void chamadaNormalNaoRegistraChamadoPorEEmContingenciaRegistra() {
        Usuario profissional = new Usuario();
        profissional.setNome("Dr. Paulo");
        Usuario recepcao = new Usuario();
        recepcao.setNome("Rita");

        Atendimento normal = novoAtendimento();
        normal.chamar(profissional, "Sala 1");
        assertSame(profissional, normal.getAtendente());
        assertNull(normal.getChamadoPor());

        Atendimento contingencia = novoAtendimento();
        contingencia.chamar(profissional, recepcao, "Sala 1");
        assertSame(profissional, contingencia.getAtendente());
        assertSame(recepcao, contingencia.getChamadoPor());
    }

    @Test
    void naoPermiteTransicoesForaDaOrdem() {
        Atendimento atendimento = novoAtendimento();

        RegraNegocioException erro = assertThrows(RegraNegocioException.class, atendimento::iniciar);
        assertEquals("Não é possível iniciar o atendimento C001: status atual é Aguardando.", erro.getMessage());

        atendimento.cancelar();
        assertEquals(StatusAtendimento.CANCELADO, atendimento.getStatus());
        assertThrows(RegraNegocioException.class, atendimento::cancelar);
    }
}
