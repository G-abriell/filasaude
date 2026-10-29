package br.com.filasaude.config;

import br.com.filasaude.entities.Perfil;
import br.com.filasaude.entities.Usuario;
import br.com.filasaude.entities.enums.Prioridade;
import br.com.filasaude.repositories.PerfilRepository;
import br.com.filasaude.repositories.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional
    public void run(String... args) {
        atualizarConstraintPrioridade();
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
            log.info("Perfil ATENDENTE renomeado para PROFISSIONAL_DE_SAUDE");
        });
    }

    /**
     * Sincroniza a constraint CHECK da coluna prioridade na tabela atendimentos
     * com todas as constantes do enum Prioridade (incluindo GESTANTE, OBESIDADE, PCD, TEA, etc.).
     */
    private void atualizarConstraintPrioridade() {
        try {
            String valoresPermitidos = Arrays.stream(Prioridade.values())
                    .map(p -> "'" + p.name() + "'")
                    .collect(Collectors.joining(", "));
            jdbcTemplate.execute("ALTER TABLE atendimentos DROP CONSTRAINT IF EXISTS atendimentos_prioridade_check");
            jdbcTemplate.execute("ALTER TABLE atendimentos ADD CONSTRAINT atendimentos_prioridade_check CHECK (prioridade IN (" + valoresPermitidos + "))");
        } catch (Exception e) {
            log.warn("Não foi possível atualizar a constraint atendimentos_prioridade_check: {}", e.getMessage());
        }
    }
}
