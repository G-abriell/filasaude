# 📋 Guia Completo do Sistema FilaSaúde: Regras de Negócio, Leis e Fluxo de Atendimento

Este documento resume **tudo o que já está implementado e funcionando no projeto FilaSaúde**, servindo como guia de alinhamento da equipe para a apresentação e para a escrita do artigo científico.

---

## 1. Posicionamento do Sistema: Clínica Médica Geral / Particular

O sistema foi estruturado para atender **Clínicas Médicas, Consultórios e Unidades de Saúde** com total controle sobre as regras de negócio:

- **Configuração da Unidade:** Já vem configurado por padrão como *"Clínica Médica Geral"* (com CNPJ, endereço e horário de funcionamento).
- **Cadastro Flexível de Pacientes:** O **CPF** e a **Data de Nascimento** são obrigatórios, enquanto o **Cartão SUS é 100% opcional** (permitindo uso imediato em clínicas particulares sem depender de regulação externa).
- **Perfis de Acesso (RBAC):**
  1. `ADMIN`: Gestão completa (usuários, perfis, filas, configurações, pacientes e atendimentos).
  2. `RECEPCAO`: Cadastro de pacientes, check-in/emissão de senhas, visualização do painel de TV e recebimento de encaminhamentos pós-consulta.
  3. `PROFISSIONAL_DE_SAUDE`: Chamada de pacientes, realização do atendimento, registro de evolução clínica, prescrição (receita) e encaminhamento.

---

## 2. Fluxo Completo: "Como o sistema sabe quem chegou primeiro?"

O fluxo do paciente dentro da clínica funciona em **4 etapas controladas pelo sistema**:

### Etapa 1 — Chegada e Check-in no Balcão da Recepção
- Ao entrar na clínica, o paciente dirige-se ao balcão da **Recepção** para fazer o seu check-in presencial.
- **Por que o check-in passa pela Recepção e não por um totem aberto?**
  1. **Validação Legal de Prioridade:** A recepcionista confere presencialmente o documento (CPF/RG ou laudo/condição de gestante/PCD), impedindo fraudes de pessoas que marcariam prioridade indevidamente em um autoatendimento sem supervisão.
  2. **Acessibilidade e Acolhimento:** Pacientes idosos (60+ e 80+) são atendidos rapidamente sem barreira tecnológica.

### Etapa 2 — Emissão da Senha e Registro do Horário Exato (`data_hora_chegada`)
- A recepcionista seleciona o paciente e a fila desejada (ex.: *Consulta Médica*, *Triagem*, *Exames*) e clica em **"Emitir Senha"**.
- **O que o sistema faz automaticamente nesse exato milissegundo:**
  1. Calcula a idade do paciente pela `data_nascimento`.
  2. Aplica automaticamente a prioridade legal (60+ ou 80+).
  3. Grava no banco de dados o campo **`data_hora_chegada`** com data, hora, minuto, segundo e milissegundo exatos (`LocalDateTime.now()`).
  4. Gera o código sequencial da senha do dia (ex.: `C001`, `C002`) e exibe na tela o **Cartão de Senha** para entrega/impressão.
  5. Impede duplicidade: se o paciente já estiver aguardando ou em atendimento, o sistema bloqueia a emissão de uma segunda senha ativa.

### Etapa 3 — Sala de Espera e Painel de TV em Tempo Real (`/painel`)
- O paciente senta na recepção acompanhando o **Painel de TV** (que fica aberto no monitor/TV da sala de espera).
- Quando o médico chama o paciente no consultório, o Painel de TV atualiza automaticamente em tempo real (a cada 4 segundos), toca o **alerta sonoro**, anuncia a senha por **voz sintetizada** e destaca a **Senha, Nome do Paciente, Prioridade e Consultório/Guichê**, além de mostrar o histórico das últimas senhas chamadas.

### Etapa 4 — Atendimento Médico, Receita e Encaminhamento
- O profissional de saúde clica em **"Iniciar"** quando o paciente entra no consultório (gravando `data_hora_inicio`).
- Ao concluir, o médico registra a **Evolução Clínica**, a **Prescrição/Receita Médica** e seleciona o **Encaminhamento para a Recepção** (ex.: *Alta liberada*, *Agendar retorno*, *Solicitar exames*), gravando `data_hora_fim`.
- O sistema calcula automaticamente o **tempo de espera** e o **tempo de consulta**.

---

## 3. Regras da Lei e Ordenação da Fila (Já Implementadas no Código)

O sistema obedece rigorosamente à legislação federal brasileira vigente (obrigatória para clínicas públicas e privadas):

| Ordem de Chamada | Categoria no Sistema | Base Legal | Como o Sistema Aplica |
| :---: | :--- | :--- | :--- |
| **1º Lugar**<br>*(Peso 0)* | **`SUPERPREFERENCIAL`** | **Lei Federal nº 13.466/2017**<br>*(Superprioridade para idosos 80+ anos)* | **Automático:** Se o paciente tem 80 anos ou mais na `data_nascimento`, o sistema atribui `SUPERPREFERENCIAL` obrigatoriamente. |
| **2º Lugar**<br>*(Peso 1)* | **`PREFERENCIAL`** | **Lei Federal nº 10.048/2000** e **Lei nº 10.741/2003**<br>*(Idosos 60+, Gestantes, Lactantes, PCD, TEA)* | **Automático para 60+ anos** (pela `data_nascimento`) ou selecionado pela recepção para Gestantes, Lactantes, PCD e TEA. |
| **3º Lugar**<br>*(Peso 2)* | **`NORMAL`** | Atendimento Geral | Aplicado aos demais pacientes. |

### Como funciona o desempate e a Regra de Intercalação 2 por 1?
1. **Desempate por Ordem Cronológica de Chegada (`data_hora_chegada ASC`):**
   Dentro do mesmo nível de prioridade, o banco de dados ordena do `data_hora_chegada` mais antigo para o mais recente. Quem fez o check-in primeiro na recepção é sempre chamado primeiro.
2. **Regra de Intercalação Automática 2 por 1 (2 Preferenciais ➔ 1 Normal):**
   Para evitar que um paciente da fila Normal fique esperando indefinidamente caso não parem de chegar pacientes preferenciais (*starvation*), o sistema aplica automaticamente a proporção **2 por 1**:
   - Chama o **1º Preferencial** (respeitando 80+ antes de 60+/Gestante/PCD);
   - Chama o **2º Preferencial**;
   - Na **3ª vez**, promove e chama automaticamente **1 paciente Normal** (se houver aguardando);
   - Reinicia o ciclo chamando mais **2 Preferenciais**, e assim sucessivamente!

---

## 4. Onde Ver Cada Regra no Código-Fonte (Mapa para a Equipe)

Caso queira abrir no IntelliJ ou VS Code para conferir ou mostrar aos professores:

| O que você quer ver | Arquivo no Projeto | Linhas Exatas |
| :--- | :--- | :---: |
| **Leis e Hierarquia de Prioridade** | `src/main/java/br/com/filasaude/entities/enums/Prioridade.java` | Linhas 1 a 34 |
| **Cálculo automático de 60+ e 80+ anos** | `src/main/java/br/com/filasaude/entities/Paciente.java` | Linhas 19–20 e 115–129 |
| **Regra de Emissão de Senha e `LocalDateTime.now()`** | `src/main/java/br/com/filasaude/services/AtendimentoService.java` | Linhas 90 a 116 |
| **Regra do Botão "Chamar Próximo"** | `src/main/java/br/com/filasaude/services/AtendimentoService.java` | Linhas 122 a 143 |
| **Consulta SQL que ordena Prioridade + Hora de Chegada** | `src/main/java/br/com/filasaude/repositories/AtendimentoRepository.java` | Linhas 34 a 87 |
| **Painel de TV em Tempo Real** | `src/main/java/br/com/filasaude/controllers/PainelController.java` | Linhas 33 a 90 |
| **Permissões de Acesso (Admin, Recepção, Médico)** | `src/main/java/br/com/filasaude/config/SecurityConfig.java` | Linhas 29 a 52 |

---

## 5. Todas as Telas que Já Estão Prontas no Sistema

1. **Login (`/login`):** Autenticação com suporte a modo claro/escuro.
2. **Dashboard Inicial (`/`):** Resumo em tempo real (quantos aguardando, em atendimento, finalizados e atalhos rápidos).
3. **Emitir Senha (`/atendimentos/novo`):** Check-in do paciente na fila e geração do Cartão de Senha.
4. **Fila de Atendimento (`/atendimentos`):** Visão completa da fila ordenada, chamada por guichê/consultório e registro clínico (evolução, receita e encaminhamento).
5. **Resumo / Prontuário do Atendimento (`/atendimentos/{id}/resumo`):** Comprovante completo da consulta e receita para impressão.
6. **Painel de Chamada para TV (`/painel`):** Tela exclusiva para a televisão da sala de espera com som e voz.
7. **Pacientes (`/pacientes`):** Cadastro, busca, edição e **Histórico Completo de Atendimentos** do paciente.
8. **Filas (`/filas`):** Criação e gerenciamento das filas da clínica (sigla, nome, ativação).
9. **Usuários (`/usuarios`) e Perfis (`/perfis`):** Administração da equipe da clínica.
10. **Configurações (`/configuracoes`) e Acessibilidade (`/acessibilidade`):** Dados da clínica, VLibras, Alto Contraste, Tamanho da Fonte, Foco Reforçado e Redução de Movimento (WCAG 2.2).

---

## 6. O que colocar como "Trabalhos Futuros" no Artigo Científico (QR Code, WhatsApp e LGPD)

Não é necessário programar nada novo para o artigo. Na seção de **Conclusão / Trabalhos Futuros**, basta mencionar a evolução planejada para próximas versões:

1. **Check-in Híbrido via QR Code e Notificações via WhatsApp:**
   - Permitir que pacientes já cadastrados façam check-in escaneando um QR Code na recepção e recebam alertas no WhatsApp quando sua vez estiver próxima.
2. **Regras de Negócio e Conformidade Legal para essa Integração Futura:**
   - **Validação de Prioridade Legal:** Pacientes com prioridade legal (Gestantes, PCD, Idosos) terão validação confirmada na recepção para evitar uso indevido do autoatendimento.
   - **Adequação à LGPD (Lei Geral de Proteção de Dados — Lei nº 13.709/2018):** Coleta de termo de consentimento expresso (*opt-in*) do paciente antes do disparo de mensagens automatizadas via WhatsApp e mascaramento de dados sensíveis de saúde em notificações externas.
   - **Chamada Intercalada Configurável (Proporção 2x1):** Parâmetro opcional nas configurações da clínica para intercalar automaticamente 1 atendimento Normal a cada 2 atendimentos Preferenciais em horários de pico.
