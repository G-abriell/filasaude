package br.com.filasaude.security;

import br.com.filasaude.services.RegistroErroService;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Detecta tentativas repetidas de login: uma senha errada é erro do usuário e não é registrada;
 * várias falhas seguidas para a mesma conta (adivinhar senha) ou vindas do mesmo endereço contra
 * várias contas (testar muitas contas) viram um alerta de segurança para o administrador.
 * Os contadores ficam em memória e zeram quando a aplicação reinicia. O endereço é o da conexão
 * recebida; atrás de um proxy reverso será o do proxy.
 */
@Component
public class MonitorTentativasLogin {

    static final int LIMITE_POR_CONTA = 5;
    static final int LIMITE_POR_ENDERECO = 10;
    static final Duration JANELA = Duration.ofMinutes(10);
    private static final int MAX_CHAVES = 5000;

    private final Map<String, Deque<Instant>> falhas = new ConcurrentHashMap<>();
    private final RegistroErroService registroErros;

    public MonitorTentativasLogin(RegistroErroService registroErros) {
        this.registroErros = registroErros;
    }

    @EventListener
    public void aoFalharLogin(AbstractAuthenticationFailureEvent evento) {
        Authentication tentativa = evento.getAuthentication();
        String conta = tentativa.getName() == null ? "?" : tentativa.getName().trim().toLowerCase();
        String endereco = tentativa.getDetails() instanceof WebAuthenticationDetails detalhes
                ? detalhes.getRemoteAddress() : "desconhecido";
        registrarFalha(conta, endereco, Instant.now());
    }

    void registrarFalha(String conta, String endereco, Instant agora) {
        podarSeNecessario(agora);
        int porConta = contar("conta|" + conta, agora);
        int porEndereco = contar("ip|" + endereco, agora);
        if (porConta >= LIMITE_POR_CONTA) {
            registroErros.seguranca("Possível adivinhação de senha: " + porConta + " falhas de login para a conta "
                    + conta + " em " + JANELA.toMinutes() + " min (último endereço: " + endereco + ").",
                    "login-conta|" + conta, "POST", "/login");
        }
        if (porEndereco >= LIMITE_POR_ENDERECO) {
            registroErros.seguranca("Possível varredura de contas: " + porEndereco + " falhas de login vindas de "
                    + endereco + " em " + JANELA.toMinutes() + " min.", "login-endereco|" + endereco, "POST", "/login");
        }
    }

    private int contar(String chave, Instant agora) {
        Deque<Instant> tentativas = falhas.computeIfAbsent(chave, k -> new ArrayDeque<>());
        synchronized (tentativas) {
            tentativas.addLast(agora);
            while (!tentativas.isEmpty() && tentativas.peekFirst().isBefore(agora.minus(JANELA))) {
                tentativas.removeFirst();
            }
            return tentativas.size();
        }
    }

    private void podarSeNecessario(Instant agora) {
        if (falhas.size() < MAX_CHAVES) {
            return;
        }
        falhas.entrySet().removeIf(entrada -> {
            Deque<Instant> tentativas = entrada.getValue();
            synchronized (tentativas) {
                return tentativas.isEmpty() || tentativas.peekLast().isBefore(agora.minus(JANELA));
            }
        });
    }
}
