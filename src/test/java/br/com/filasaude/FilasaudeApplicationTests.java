package br.com.filasaude;

import br.com.filasaude.entities.Atendimento;
import br.com.filasaude.entities.Fila;
import br.com.filasaude.entities.Paciente;
import br.com.filasaude.entities.Usuario;
import br.com.filasaude.entities.enums.Prioridade;
import br.com.filasaude.entities.enums.StatusAtendimento;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
}
