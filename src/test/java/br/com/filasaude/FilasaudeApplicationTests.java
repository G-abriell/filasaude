package br.com.filasaude;

import br.com.filasaude.entities.Atendimento;
import br.com.filasaude.entities.Fila;
import br.com.filasaude.entities.Paciente;
import br.com.filasaude.entities.Usuario;
import br.com.filasaude.entities.enums.Prioridade;
import br.com.filasaude.entities.enums.StatusAtendimento;
import br.com.filasaude.services.AtendimentoService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilasaudeApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void deveNormalizarCpfEFormatarTelefoneNoPadraoCelular() {
        Paciente paciente = new Paciente();
        paciente.setNome("Maria Silva");
        paciente.setCpf("123.456.789-00");
        paciente.setCartaoSus("123 4567 8901 2345");
        paciente.setTelefone("44988887777");
        paciente.setDataNascimento(LocalDate.now().minusYears(65));

        assertEquals("12345678900", paciente.getCpf());
        assertEquals("123.456.789-00", paciente.getCpfFormatado());
        assertEquals("123456789012345", paciente.getCartaoSus());
        assertEquals("(44) 9 8888-7777", paciente.getTelefone());
        assertEquals("(44) 9 8888-7777", paciente.getTelefoneFormatado());
        assertTrue(paciente.isIdoso());
        assertFalse(paciente.isSuperIdoso());
    }

    @Test
    void deveIdentificarSuperprioridade80AnosConformeLei13466() {
        Paciente idoso85 = new Paciente();
        idoso85.setNome("Sebastião Ferreira");
        idoso85.setCpf("11122233344");
        idoso85.setDataNascimento(LocalDate.now().minusYears(85));

        assertTrue(idoso85.isIdoso());
        assertTrue(idoso85.isSuperIdoso());

        Paciente idoso68 = new Paciente();
        idoso68.setNome("Francisca Neves");
        idoso68.setCpf("22233344455");
        idoso68.setDataNascimento(LocalDate.now().minusYears(68));

        assertTrue(idoso68.isIdoso());
        assertFalse(idoso68.isSuperIdoso());

        // Verificação da hierarquia de ordenação legal: Superpreferencial (1) < Preferencial (2) < Normal (3)
        assertTrue(Prioridade.SUPERPREFERENCIAL.getOrdem() < Prioridade.PREFERENCIAL.getOrdem());
        assertTrue(Prioridade.PREFERENCIAL.getOrdem() < Prioridade.NORMAL.getOrdem());
    }

    @Test
    void deveRegistrarEvolucaoPrescricaoEEncaminhamentoParaRecepcao() {
        Paciente paciente = new Paciente();
        paciente.setNome("Carlos Souza");
        paciente.setCpf("98765432100");
        paciente.setDataNascimento(LocalDate.now().minusYears(40));

        Fila fila = new Fila();
        fila.setNome("Consulta Médica");
        fila.setSigla("C");

        Usuario medico = new Usuario();
        medico.setNome("Dr. Roberto");

        Atendimento atendimento = new Atendimento(paciente, fila, Prioridade.NORMAL, 1, LocalDateTime.now().minusMinutes(15));
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
    void deveIntercalarDoisPreferenciaisParaUmNormalNaFila() {
        Fila fila = new Fila();
        fila.setNome("Consulta Médica");
        fila.setSigla("C");

        LocalDateTime base = LocalDateTime.now().minusMinutes(30);
        Paciente p1 = new Paciente();
        p1.setNome("Idoso 1");
        Paciente p2 = new Paciente();
        p2.setNome("Idoso 2");
        Paciente p3 = new Paciente();
        p3.setNome("Gestante 3");
        Paciente p4 = new Paciente();
        p4.setNome("PCD 4");
        Paciente n1 = new Paciente();
        n1.setNome("João Normal 1");
        Paciente n2 = new Paciente();
        n2.setNome("Maria Normal 2");

        Atendimento pref1 = new Atendimento(p1, fila, Prioridade.SUPERPREFERENCIAL, 2, base.plusMinutes(2));
        Atendimento pref2 = new Atendimento(p2, fila, Prioridade.PREFERENCIAL, 3, base.plusMinutes(4));
        Atendimento pref3 = new Atendimento(p3, fila, Prioridade.GESTANTE, 4, base.plusMinutes(6));
        Atendimento pref4 = new Atendimento(p4, fila, Prioridade.PCD, 5, base.plusMinutes(8));
        Atendimento norm1 = new Atendimento(n1, fila, Prioridade.NORMAL, 1, base);
        Atendimento norm2 = new Atendimento(n2, fila, Prioridade.NORMAL, 6, base.plusMinutes(10));

        List<Atendimento> aguardandoOrdenado = List.of(pref1, pref2, pref3, pref4, norm1, norm2);

        // 0 preferenciais chamados antes: deve ordenar [Pref1, Pref2, Norm1, Pref3, Pref4, Norm2]
        List<Atendimento> cicloZero = AtendimentoService.intercalarFila2Por1(aguardandoOrdenado, 0);
        assertEquals(List.of(pref1, pref2, norm1, pref3, pref4, norm2), cicloZero);

        // 1 preferencial já chamado antes: falta 1 preferencial e depois 1 normal [Pref2, Norm1, Pref3, Pref4, Norm2]
        List<Atendimento> aposPrimeiroPref = AtendimentoService.intercalarFila2Por1(
                List.of(pref2, pref3, pref4, norm1, norm2), 1);
        assertEquals(List.of(pref2, norm1, pref3, pref4, norm2), aposPrimeiroPref);

        // 2 preferenciais já chamados seguidos: o próximo (1º da fila) DEVE ser o Normal (João!) [Norm1, Pref3, Pref4, Norm2]
        List<Atendimento> aposSegundoPref = AtendimentoService.intercalarFila2Por1(
                List.of(pref3, pref4, norm1, norm2), 2);
        assertEquals(List.of(norm1, pref3, pref4, norm2), aposSegundoPref);
    }
}
