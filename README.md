# FilaSaúde — Gestão de Atendimento

O FilaSaúde é um sistema web desenvolvido como projeto acadêmico na disciplina de **PBL (Problem-Based Learning)**. O trabalho está alinhado ao tema semestral:

> “Soluções Digitais como Meio de Transformação de Processos”

## Objetivo

O projeto propõe apoiar a organização do acolhimento e da triagem em unidades de saúde. A aplicação busca:

- Organizar e apresentar as filas de espera.
- Facilitar a chamada de pacientes.
- Reunir o histórico de atendimentos e indicadores básicos de tempo.

## Tecnologias

- **Linguagem:** Java 17 ou superior
- **Framework:** Spring Boot 3
- **Web e páginas:** Spring MVC e Thymeleaf
- **Persistência:** Spring Data JPA e Hibernate
- **Banco de dados:** PostgreSQL
- **Estilos:** Bootstrap 5

## Executar localmente

### Pré-requisitos

- JDK 17 ou superior instalado.
- PostgreSQL instalado e em execução na porta `5432`.
- Git instalado.

### 1. Baixar o projeto

```bash
git clone https://github.com/G-abriell/filasaude.git
cd filasaude
```

### 2. Configurar o banco de dados

As configurações de conexão com o PostgreSQL ficam em `src/main/resources/application.properties`. Confira os valores de conexão e ajuste-os para o seu ambiente antes de iniciar a aplicação. O esquema SQL disponibilizado pelo projeto está em `src/main/resources/schema_sql/ddl_filasaude_pbl.sql`.

### 3. Iniciar a aplicação

No Windows, execute:

```powershell
.\mvnw.cmd spring-boot:run
```

No Linux ou macOS, execute:

```bash
./mvnw spring-boot:run
```

Quando a inicialização terminar, acesse [http://localhost:8080](http://localhost:8080) no navegador.

## Executar os testes

Para executar os testes automatizados do projeto:

```bash
./mvnw test
```

No Windows, use `.\mvnw.cmd test` no PowerShell.
