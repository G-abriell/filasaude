package br.com.filasaude.config;

import br.com.filasaude.entities.Perfil;
import br.com.filasaude.entities.Usuario;
import br.com.filasaude.entities.enums.Prioridade;
import br.com.filasaude.entities.enums.TipoErro;
import br.com.filasaude.repositories.PerfilRepository;
import br.com.filasaude.repositories.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Cria os perfis padrão e o usuário administrador na primeira execução.
 * As credenciais do admin vêm de filasaude.admin.* (application.properties / variáveis de ambiente).
 */
@Component
public class DadosIniciais implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DadosIniciais.class);

    private static final Map<String, String> PERFIS = new LinkedHashMap<>();

    static {
        PERFIS.put("ADMIN", "Administrador do sistema");
        PERFIS.put("RECEPCAO", "Recepção: cadastro de pacientes, emissão de senhas e encaminhamentos");
        PERFIS.put("PROFISSIONAL_DE_SAUDE", "Profissional de saúde: chamada de pacientes, registro clínico e prescrições");
    }

    private final PerfilRepository perfilRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Value("${filasaude.admin.nome:Administrador}")
    private String adminNome;

    @Value("${filasaude.admin.email:}")
    private String adminEmail;

    @Value("${filasaude.admin.senha:}")
    private String adminSenha;

    public DadosIniciais(PerfilRepository perfilRepository, UsuarioRepository usuarioRepository,
                         PasswordEncoder passwordEncoder, JdbcTemplate jdbcTemplate) {
        this.perfilRepository = perfilRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Sem transação única de propósito: se o ajuste da constraint falhar, o PostgreSQL abortaria
     * a transação inteira e impediria a criação dos perfis e do administrador logo em seguida.
     */
    @Override
    public void run(String... args) {
        sincronizarConstraintEnum("atendimentos", "prioridade", Prioridade.values(), false);
        sincronizarConstraintEnum("registros_erro", "tipo", TipoErro.values(), true);
        renomearPerfilAtendente();

        PERFIS.forEach((nome, descricao) -> {
            if (perfilRepository.findByNomeIgnoreCase(nome).isEmpty()) {
                perfilRepository.save(new Perfil(null, nome, descricao));
                log.info("Perfil {} criado", nome);
            }
        });

        if (adminEmail.isBlank() || usuarioRepository.findByEmailIgnoreCase(adminEmail.trim()).isPresent()) {
            return;
        }
        if (adminSenha.isBlank()) {
            log.warn("Usuário admin não criado: defina filasaude.admin.senha (ou a variável FILASAUDE_ADMIN_SENHA)");
            return;
        }
        Perfil admin = perfilRepository.findByNomeIgnoreCase("ADMIN").orElseThrow();
        usuarioRepository.save(new Usuario(null, adminNome, adminEmail.trim().toLowerCase(),
                passwordEncoder.encode(adminSenha), admin));
        log.info("Usuário administrador {} criado", adminEmail);
    }

    /**
     * O antigo perfil ATENDENTE passou a se chamar PROFISSIONAL_DE_SAUDE.
     * Renomeia o registro existente para que os usuários vinculados mantenham o acesso.
     */
    private void renomearPerfilAtendente() {
        if (perfilRepository.findByNomeIgnoreCase("PROFISSIONAL_DE_SAUDE").isPresent()) {
            return;
        }
        perfilRepository.findByNomeIgnoreCase("ATENDENTE").ifPresent(perfil -> {
            perfil.setNome("PROFISSIONAL_DE_SAUDE");
            perfil.setDescricao(PERFIS.get("PROFISSIONAL_DE_SAUDE"));
            perfilRepository.save(perfil);
            log.info("Perfil ATENDENTE renomeado para PROFISSIONAL_DE_SAUDE");
        });
    }

    /**
     * O Hibernate cria uma constraint CHECK com os valores de cada enum no momento em que a tabela nasce e
     * o ddl-auto=update não a atualiza. Esta sincronização a recria com todas as constantes atuais do enum
     * (valores só vêm do código, nunca de entrada do usuário).
     *
     * @param removerLinhasInvalidas apaga linhas com valores que o enum não tem mais; só para tabelas
     *                               descartáveis, como o registro de erros, nunca para dados do negócio
     */
    private void sincronizarConstraintEnum(String tabela, String coluna, Enum<?>[] valores, boolean removerLinhasInvalidas) {
        String restricao = tabela + "_" + coluna + "_check";
        try {
            String permitidos = Arrays.stream(valores).map(v -> "'" + v.name() + "'").collect(Collectors.joining(", "));
            if (removerLinhasInvalidas) {
                jdbcTemplate.update("DELETE FROM " + tabela + " WHERE " + coluna + " NOT IN (" + permitidos + ")");
            }
            jdbcTemplate.execute("ALTER TABLE " + tabela + " DROP CONSTRAINT IF EXISTS " + restricao);
            jdbcTemplate.execute("ALTER TABLE " + tabela + " ADD CONSTRAINT " + restricao
                    + " CHECK (" + coluna + " IN (" + permitidos + "))");
        } catch (Exception e) {
            log.warn("Não foi possível atualizar a constraint {}: {}", restricao, e.getMessage());
        }
    }
}
