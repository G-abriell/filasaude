# Contexto do projeto

## Propósito e escopo
FilaSaúde: sistema web acadêmico (PBL) para organizar filas, chamada de pacientes e histórico de atendimentos em unidades de saúde. Regras permanentes de trabalho: [AGENTS.md](../AGENTS.md). Uso e execução: [README.md](../README.md).

## Arquitetura e ambiente
- Spring Boot 4.1.1, Java 21 (`pom.xml`), Spring MVC + Thymeleaf, Spring Data JPA, Spring Security (form login, BCrypt), PostgreSQL (`filasaude_db`, `ddl-auto=update`).
- Pacotes em `br.com.filasaude`: `config`, `controllers`, `entities` (+ `enums`), `repositories`, `services`, `security`, `exceptions`. Telas em `src/main/resources/templates`; CSS/JS em `static/`.
- Perfis (roles): `ADMIN`, `RECEPCAO`, `PROFISSIONAL_DE_SAUDE`; autorização por URL em `config/SecurityConfig.java`.
- Fila de espera: ordem por prioridade legal (`Prioridade.ordem`) e chegada, com intercalação 2 preferenciais : 1 normal (`AtendimentoService.intercalarFila2Por1`). Concorrência de guichês resolvida com lock pessimista nas filas (`FilaRepository`).
- Preferências de acessibilidade e a tela de configurações da unidade ficam em memória (não persistem); detalhes em [STATE.md](STATE.md).

## Restrições e fontes
- Não alterar dados reais do PostgreSQL local sem autorização; o teste `FilasaudeApplicationTests` usa esse banco.
- Sem operações Git (commit, push, merge) sem autorização explícita.
- O DDL de referência `src/main/resources/schema_sql/ddl_filasaude_pbl.sql` foi conferido com as entidades em 2026-11-02 e está consistente.
