# FilaSaúde — Instruções permanentes do agente

## 1. Contexto do projeto

Projeto acadêmico FilaSaúde, desenvolvido com Java 17+, Spring Boot 3, Spring MVC, Thymeleaf, Spring Data JPA, PostgreSQL e Bootstrap 5.

O objetivo é evoluir o software com simplicidade, consistência, segurança e facilidade de manutenção, preservando a arquitetura existente.

## 2. Economia de recursos

- Utilize esforço de raciocínio baixo ou econômico nas tarefas simples e localizadas.
- Utilize esforço médio nas implementações que envolvam vários arquivos ou regras de negócio.
- Reserve esforço alto para bugs complexos, decisões arquiteturais e problemas difíceis de diagnosticar.
- Não solicite análises completas do repositório quando o escopo for específico.
- Consulte apenas os arquivos, dependências e testes relevantes para a tarefa.
- Evite repetir informações já conhecidas e não gere explicações extensas sem necessidade.
- Não sacrifique a qualidade, a segurança ou a correção para economizar recursos.

## 3. Regras de implementação

- Antes de alterar o código, identifique o comportamento atual e os arquivos envolvidos.
- Preserve a arquitetura, as tecnologias e os padrões já adotados no projeto.
- Prefira soluções convencionais, simples e fáceis de explicar em uma apresentação acadêmica.
- Evite novas dependências, abstrações e camadas sem justificativa técnica.
- Faça alterações pequenas, coesas e limitadas ao escopo solicitado.
- Não reescreva arquivos inteiros quando uma alteração localizada for suficiente.
- Não modifique funcionalidades fora do escopo sem necessidade justificada.
- Não invente requisitos, dados, regras de negócio ou funcionalidades.
- Não substitua implementações existentes sem verificar os impactos.

## 4. Banco de dados e segurança

- Preserve a compatibilidade com PostgreSQL e com o modelo de dados existente.
- Verifique o impacto de alterações em entidades, relacionamentos, repositórios e consultas.
- Não execute operações destrutivas nem altere dados reais sem autorização explícita.
- Não exponha credenciais, senhas, tokens ou dados pessoais em logs e respostas.
- Utilize validação de entrada e os mecanismos de segurança apropriados ao contexto.
- Não armazene senhas em texto puro nem desative proteções para facilitar a implementação.

## 5. Testes e validação

- Execute as verificações pertinentes às alterações realizadas.
- Para mudanças em regras de negócio, priorize testes que comprovem o comportamento esperado.
- Para alterações em persistência, verifique as integrações afetadas sempre que possível.
- Não afirme que os testes passaram se não tiver executado as verificações.
- Se algum teste não puder ser executado, informe o motivo.
- Corrija regressões introduzidas pela própria alteração antes de concluir a tarefa.

## 6. Controle de escopo e Git

- Não descarte, sobrescreva ou reverta alterações preexistentes do usuário.
- Não execute commits, merges, pushes ou operações destrutivas sem autorização explícita.
- Antes de operações Git relevantes, verifique o estado do repositório.
- Não altere arquivos de configuração ou dependências sem necessidade.

## 7. Comunicação final

Ao concluir cada tarefa, informe de forma objetiva:

1. O que foi implementado.
2. Quais arquivos foram modificados.
3. Quais testes ou verificações foram executados e seus resultados.
4. Quais limitações ou pendências permaneceram.

Se faltar uma informação essencial, faça uma pergunta objetiva antes de prosseguir. Caso contrário, execute a tarefa sem interromper o fluxo com perguntas desnecessárias.

## 8. Princípio geral

Priorize a solução mais simples que atenda corretamente ao requisito, respeite o código existente e possa ser compreendida e mantida por um estudante. Não faça refatorações ou melhorias adicionais fora do escopo sem necessidade.
