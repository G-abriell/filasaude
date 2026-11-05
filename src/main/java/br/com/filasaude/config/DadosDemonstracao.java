package br.com.filasaude.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Carrega dados fictícios para demonstração (filas, pacientes e atendimentos do dia).
 * Só roda com FILASAUDE_DEMO_DADOS=true; o script ignora registros que já existem.
 */
@Component
@Order(2) // depois do DadosIniciais (perfis e admin)
public class DadosDemonstracao implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DadosDemonstracao.class);

    private final DataSource dataSource;

    @Value("${filasaude.demo.dados:false}")
    private boolean ativo;

    public DadosDemonstracao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {
        if (!ativo) {
            return;
        }
        try {
            new ResourceDatabasePopulator(new ClassPathResource("demo/dados_demo.sql")).execute(dataSource);
            log.info("Dados de demonstração carregados");
        } catch (Exception e) {
            log.warn("Não foi possível carregar os dados de demonstração: {}", e.getMessage());
        }
    }
}
