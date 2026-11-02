package br.com.filasaude;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Teste de integração: sobe a aplicação completa e, por isso, precisa do PostgreSQL configurado em
 * application.properties em execução. Os testes de regras de negócio ficam em classes unitárias
 * (sem Spring e sem banco) nos pacotes entities e services.
 */
@SpringBootTest
class FilasaudeApplicationTests {

    @Test
    void contextLoads() {
    }
}
