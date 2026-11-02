package br.com.filasaude.security;

import br.com.filasaude.services.RegistroErroService;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MonitorTentativasLoginTest {

    private final RegistroErroService registro = mock(RegistroErroService.class);
    private final MonitorTentativasLogin monitor = new MonitorTentativasLogin(registro);
    private final Instant inicio = Instant.parse("2026-11-02T12:00:00Z");

    @Test
    void poucasFalhasSaoErroDoUsuarioENaoViramAlerta() {
        for (int i = 0; i < MonitorTentativasLogin.LIMITE_POR_CONTA - 1; i++) {
            monitor.registrarFalha("rita@x.com", "10.0.0.1", inicio.plusSeconds(i));
        }

        verifyNoInteractions(registro);
    }

    @Test
    void falhasRepetidasNaMesmaContaGeramAlertaDeAdivinhacaoDeSenha() {
        for (int i = 0; i < MonitorTentativasLogin.LIMITE_POR_CONTA; i++) {
            monitor.registrarFalha("rita@x.com", "10.0.0." + i, inicio.plusSeconds(i));
        }

        verify(registro, times(1)).seguranca(contains("adivinhação de senha"), eq("login-conta|rita@x.com"),
                eq("POST"), eq("/login"));
    }

    @Test
    void falhasDeContasDiferentesDoMesmoEnderecoGeramAlertaDeVarredura() {
        for (int i = 0; i < MonitorTentativasLogin.LIMITE_POR_ENDERECO; i++) {
            monitor.registrarFalha("conta" + i + "@x.com", "10.9.9.9", inicio.plusSeconds(i));
        }

        verify(registro, times(1)).seguranca(contains("varredura de contas"), eq("login-endereco|10.9.9.9"),
                eq("POST"), eq("/login"));
        verify(registro, never()).seguranca(contains("adivinhação"), anyString(), anyString(), anyString());
    }

    @Test
    void falhasForaDaJanelaNaoSeAcumulam() {
        for (int i = 0; i < MonitorTentativasLogin.LIMITE_POR_CONTA - 1; i++) {
            monitor.registrarFalha("rita@x.com", "10.0.0.1", inicio.plusSeconds(i));
        }
        // 20 minutos depois, uma nova falha começa uma contagem nova
        monitor.registrarFalha("rita@x.com", "10.0.0.1", inicio.plus(MonitorTentativasLogin.JANELA).plusSeconds(600));

        verifyNoInteractions(registro);
    }
}
