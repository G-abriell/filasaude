# Handoff

Ver [estado atual](STATE.md) e [contexto](CONTEXT.md). Regras ativas: [AGENTS.md](../AGENTS.md).

## Estado verificado
Revisão sênior concluída no código Java, testes, `pom.xml`, README e CSS externo; 31 testes passam, incluindo integração em banco descartável, e a aplicação foi exercitada por HTTP (ver [STATE.md](STATE.md)). Nada foi commitado. Alterações locais do usuário em `src/main/resources/templates/painel.html`, `static/css/painel-demonstracao.css` e a exclusão de `GUIA_REGRAS_DE_NEGOCIO_E_FLUXO.md`, `PRIMEIRA_VERSAO.md` e `REGRAS_DE_NEGOCIO.md` foram preservadas.

## Próxima ação
Abrir a aplicação num navegador (`.\mvnw.cmd spring-boot:run`) e conferir visualmente `src/main/resources/templates/atendimentos/guiche.html`, o menu e as telas de perfis RECEPCAO e PROFISSIONAL_DE_SAUDE após a remoção de CSS morto em `static/css/filasaude.css`. Resultado esperado (expected): telas sem regressão visual e mensagem de erro de regra de negócio visível na própria tela.

## Critérios de aceite
Telas de guichê, recepção e usuários sem diferença visual em relação à versão anterior; mensagens de erro visíveis; os testes seguem em 31 passando (`.\mvnw.cmd test`, preferindo um banco descartável via `SPRING_DATASOURCE_URL`).

## Restrições e bloqueios
- Não rodar o `@SpringBootTest` nem a aplicação contra `filasaude_db` sem autorização: alteram o schema/dados do banco local. Use um banco descartável (`SPRING_DATASOURCE_URL`).
- Débitos mantidos de propósito (ver [STATE.md](STATE.md)): tela de configurações sem efeito no sistema; CSS/JS embutidos em `index.html`, `configuracoes.html` e `painel.html`. Só mexer neles com verificação visual no navegador.
- Não executar operações Git sem autorização.

## Evidência
- `.\mvnw.cmd -o -B test "-Dtest=!FilasaudeApplicationTests"`: 30 testes, 0 falhas, BUILD SUCCESS (2026-11-02).
- Testes novos: `AtendimentoServiceTest` (ordem das travas e da fila), `UsuarioServiceTest`, `GlobalExceptionHandlerTest`, `PreferenciasAcessibilidadeTest`, `PacienteTest`, `AtendimentoTest`.
