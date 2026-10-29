# ⚖️ Documento de Regras de Negócio (RN) — Sistema FilaSaúde

## O que são "Regras de Negócio"?
Em Engenharia de Software, **Regras de Negócio (RN)** são as **condições, leis, restrições e validações do mundo real** que o sistema é programado para impor e respeitar. 

Enquanto um *Requisito Funcional* diz **o que** o sistema faz (ex.: *"O sistema permite emitir senha"*), a *Regra de Negócio* define **quais são as leis e travas** dessa ação (ex.: *"Um paciente não pode tirar duas senhas ao mesmo tempo"* ou *"Acima de 80 anos a senha é obrigatoriamente Superpreferencial"*).

---

## 1. Regras de Negócio Já Implementadas no Sistema (`RN01` a `RN15`)

### 📁 Grupo A — Cadastro de Pacientes e Filas

| Código | Nome da Regra | Descrição da Regra no Sistema | Onde está no Código |
| :---: | :--- | :--- | :--- |
| **RN01** | **Unicidade de CPF e Cartão SUS** | O sistema não permite cadastrar dois pacientes com o mesmo CPF ou com o mesmo número de Cartão SUS. | `PacienteService.java` *(linhas 48–62)* |
| **RN02** | **Identificação para Clínica Particular / Geral** | O CPF (11 dígitos) e a Data de Nascimento são obrigatórios. O Cartão SUS (15 dígitos) é opcional, permitindo uso em clínicas particulares. | `Paciente.java` *(linhas 31–44)* |
| **RN03** | **Validação de Data de Nascimento** | A data de nascimento do paciente deve ser obrigatoriamente uma data passada (`@Past`). | `Paciente.java` *(linha 41)* |
| **RN04** | **Unicidade de Nome e Sigla da Fila** | Não é permitido criar duas filas com o mesmo nome ou com a mesma sigla (ex.: duas filas com a letra `C`). | `FilaService.java` *(linhas 40–55)* |
| **RN05** | **Preservação de Histórico Clínico** | Pacientes, filas ou usuários que já possuem atendimentos registrados no histórico não podem ser excluídos do banco de dados (apenas inativados), preservando a integridade do prontuário. | `PacienteService.java` *(linha 69)*<br>`FilaService.java` *(linha 68)*<br>`UsuarioService.java` *(linha 91)* |

---

### 🎫 Grupo B — Emissão de Senha e Prioridade Legal (Check-in na Recepção)

| Código | Nome da Regra | Descrição da Regra no Sistema | Onde está no Código |
| :---: | :--- | :--- | :--- |
| **RN06** | **Bloqueio de Fila Inativa** | O sistema impede a emissão de novas senhas ou chamadas para filas que estejam marcadas como inativas. | `AtendimentoService.java` *(linhas 95–97)* |
| **RN07** | **Proibição de Duplicidade de Senha Ativa** | Um paciente não pode retirar uma nova senha se já possuir um atendimento ativo (`AGUARDANDO`, `CHAMADO` ou `EM_ATENDIMENTO`). | `AtendimentoService.java` *(linhas 98–100)* |
| **RN08** | **Superprioridade Automática (80+ anos)** | Em cumprimento à **Lei Federal nº 13.466/2017**, todo paciente com idade igual ou superior a 80 anos recebe automaticamente a prioridade `SUPERPREFERENCIAL`, independentemente da seleção manual na tela. | `AtendimentoService.java` *(linhas 103–104)*<br>`Paciente.java` *(linha 127)* |
| **RN09** | **Prioridade Preferencial Automática (60+ anos) e Legal** | Em cumprimento à **Lei Federal nº 10.048/2000** e ao **Estatuto da Pessoa Idosa (Lei nº 10.741/2003)**, pacientes com 60 anos ou mais recebem automaticamente prioridade `PREFERENCIAL` (podendo também ser atribuída a Gestantes, Lactantes, PCD e TEA). | `AtendimentoService.java` *(linhas 105–110)*<br>`Paciente.java` *(linha 123)* |
| **RN10** | **Numeração Diária e Timestamp de Chegada** | A numeração das senhas (`C001`, `C002`...) reinicia diariamente para cada fila (`data_referencia`) e registra o instante exato de chegada (`data_hora_chegada = LocalDateTime.now()`). | `AtendimentoService.java` *(linhas 112–115)*<br>`Atendimento.java` *(linhas 91–100)* |

---

### 🩺 Grupo C — Chamada, Ciclo de Vida do Atendimento e Segurança

| Código | Nome da Regra | Descrição da Regra no Sistema | Onde está no Código |
| :---: | :--- | :--- | :--- |
| **RN11** | **Algoritmo de Ordenação e Intercalação 2 por 1 da Fila** | A chamada respeita a hierarquia legal (**1º `SUPERPREFERENCIAL` 80+ ➔ 2º `PREFERENCIAL` / Gestante / PCD / TEA ➔ 3º `NORMAL`**) com desempate por horário de chegada (`data_hora_chegada ASC`), aplicando a **Regra de Intercalação 2x1**: a cada **2 pacientes Preferenciais** chamados, o sistema intercala automaticamente **1 paciente Normal** (se houver aguardando), impedindo que a fila normal fique travada indefinidamente. | `AtendimentoService.java` *(linhas 62–130)*<br>`AtendimentoRepository.java` *(linhas 32–76)* |
| **RN12** | **Máquina de Estados do Atendimento** | Um atendimento só pode seguir transições válidas:<br>• Só pode ser **Chamado** se estiver `AGUARDANDO`.<br>• Só pode ser **Iniciado** ou marcado como **Não Compareceu** se estiver `CHAMADO`.<br>• Só pode ser **Finalizado** se já tiver sido iniciado (`EM_ATENDIMENTO`). | `Atendimento.java` *(linhas 104–176)* |
| **RN13** | **Rechamada no Painel de TV** | O profissional pode acionar a **Rechamada** de um paciente que está com status `CHAMADO`, atualizando o `data_hora_chamada` para disparar novamente o alerta sonoro e visual na TV da sala de espera. | `Atendimento.java` *(linhas 119–127)* |
| **RN14** | **Registro Clínico e Encaminhamento para Recepção** | Ao finalizar a consulta, o profissional registra a evolução clínica, a receita médica (`prescricao`) e a orientação de saída (`encaminhamento`), que fica imediatamente visível para a Recepção orientar o paciente. | `Atendimento.java` *(linhas 139–149)* |
| **RN15** | **Segregação de Funções por Perfil (RBAC)** | • Somente `ADMIN` e `RECEPCAO` podem cadastrar pacientes e emitir senhas.<br>• Somente `ADMIN` pode gerenciar usuários, perfis e configurações.<br>• Qualquer usuário autenticado (`ADMIN`, `RECEPCAO`, `PROFISSIONAL_DE_SAUDE`) pode operar chamadas e abrir o Painel de TV. | `SecurityConfig.java` *(linhas 33–39)* |

---

## 2. Regras de Negócio para a Integração Futura (QR Code e WhatsApp)
*(Para incluir na seção de Trabalhos Futuros / Evolução do Sistema no Artigo Científico)*

| Código | Nome da Regra | Descrição da Regra Planejada | Base Legal / Justificativa |
| :---: | :--- | :--- | :--- |
| **RN-FUT-01** | **Validação de Prioridade no Check-in Digital** | Caso o paciente solicite senha Preferencial ou Superpreferencial via QR Code no celular, a senha entra com status de *confirmação documental pendente* no balcão da recepção antes da chamada. | Evita fraudes no autoatendimento e garante cumprimento idôneo das Leis nº 10.048/2000 e nº 13.466/2017. |
| **RN-FUT-02** | **Consentimento e Privacidade no WhatsApp (LGPD)** | O envio de notificações de posição na fila pelo WhatsApp exige aceite explícito (*opt-in*) do paciente, e as mensagens enviadas não devem expor diagnósticos, receitas ou especialidades médicas sensíveis. | **Lei Geral de Proteção de Dados (Lei nº 13.709/2018 — Art. 11, Dados Sensíveis de Saúde)**. |
| **RN-FUT-03** | **Limite de Tolerância para Chamada Remota** | Pacientes notificados via WhatsApp possuem uma janela de tolerância após o status `CHAMADO`; caso não se apresentem ao consultório após rechamada, o status transita para `NAO_COMPARECEU`. | Evita ociosidade do médico no consultório. |
| **RN-FUT-04** | **Regra de Intercalação Configurável (Proporção 2x1)** | Em horários de alta demanda, o sistema poderá alternar automaticamente a chamada na proporção de **2 pacientes Preferenciais para 1 paciente Normal**, evitando tempo de espera excessivo (*starvation*) da fila normal. | Equilíbrio operacional do fluxo da clínica mantendo o atendimento prioritário. |
