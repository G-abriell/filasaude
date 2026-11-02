package br.com.filasaude.entities;

import br.com.filasaude.entities.enums.Prioridade;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PacienteTest {

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
    void deveFormatarTelefoneFixoETratarValoresVazios() {
        Paciente paciente = new Paciente();

        paciente.setTelefone("4433334444");
        assertEquals("(44) 3333-4444", paciente.getTelefone());

        paciente.setTelefone("12345");
        assertEquals("12345", paciente.getTelefone());

        paciente.setTelefone("   ");
        assertNull(paciente.getTelefone());
        assertNull(paciente.getTelefoneFormatado());

        paciente.setCartaoSus("  ");
        assertNull(paciente.getCartaoSus());
    }

    @Test
    void deveIdentificarSuperprioridade80AnosConformeLei13466() {
        Paciente idoso85 = new Paciente();
        idoso85.setDataNascimento(LocalDate.now().minusYears(85));
        assertTrue(idoso85.isIdoso());
        assertTrue(idoso85.isSuperIdoso());

        Paciente idoso68 = new Paciente();
        idoso68.setDataNascimento(LocalDate.now().minusYears(68));
        assertTrue(idoso68.isIdoso());
        assertFalse(idoso68.isSuperIdoso());

        // Hierarquia legal: Superpreferencial (1) < Preferencial (2) < Normal (3)
        assertTrue(Prioridade.SUPERPREFERENCIAL.getOrdem() < Prioridade.PREFERENCIAL.getOrdem());
        assertTrue(Prioridade.PREFERENCIAL.getOrdem() < Prioridade.NORMAL.getOrdem());
    }
}
