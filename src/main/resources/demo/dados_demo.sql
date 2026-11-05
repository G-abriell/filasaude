-- Dados FICTÍCIOS para demonstração do FilaSaúde (PostgreSQL).
-- Pode ser executado mais de uma vez: ignora registros que já existem.
-- Executado pelo DadosDemonstracao quando FILASAUDE_DEMO_DADOS=true.


-- Filas
INSERT INTO filas (nome, sigla, descricao, ativa) VALUES
  ('Triagem',       'T', 'Classificação inicial dos pacientes', true),
  ('Clínico Geral', 'C', 'Consultas de clínica geral',          true),
  ('Pediatria',     'P', 'Atendimento infantil',                true),
  ('Exames',        'E', 'Coleta e exames laboratoriais',       true)
ON CONFLICT DO NOTHING;

-- Pacientes (CPFs e Cartões SUS inventados)
INSERT INTO pacientes (nome, cpf, cartao_sus, data_nascimento, telefone, data_cadastro) VALUES
  ('Maria das Graças Oliveira', '11111111101', '700000000000001', '1941-03-12', '(11) 9 1111-0001', now()),
  ('José Carlos Pereira',       '11111111102', '700000000000002', '1948-07-30', '(11) 9 1111-0002', now()),
  ('Ana Beatriz Souza',         '11111111103', '700000000000003', '1995-11-02', '(11) 9 1111-0003', now()),
  ('Carlos Eduardo Lima',       '11111111104', '700000000000004', '1987-05-19', '(11) 9 1111-0004', now()),
  ('Fernanda Rocha',            '11111111105', '700000000000005', '1992-09-08', '(11) 9 1111-0005', now()),
  ('Lucas Almeida',             '11111111106', '700000000000006', '2018-01-25', '(11) 9 1111-0006', now()),
  ('Sofia Martins',             '11111111107', '700000000000007', '2020-06-14', '(11) 9 1111-0007', now()),
  ('Pedro Henrique Costa',      '11111111108', '700000000000008', '2001-12-03', '(11) 9 1111-0008', now()),
  ('Helena Barbosa',            '11111111109', '700000000000009', '1969-04-21', '(11) 9 1111-0009', now()),
  ('Antônio Ferreira',          '11111111110', '700000000000010', '1955-08-17', '(11) 9 1111-0010', now()),
  ('Juliana Ribeiro',           '11111111111', '700000000000011', '1998-02-27', '(11) 9 1111-0011', now()),
  ('Rafael Nogueira',           '11111111112', '700000000000012', '1983-10-09', '(11) 9 1111-0012', now())
ON CONFLICT DO NOTHING;

-- Atendimentos de hoje, em vários estados do ciclo de vida.
-- Colunas de tempo = "minutos atrás" (NULL = ainda não ocorreu).
INSERT INTO atendimentos
  (paciente_id, fila_id, atendente_id, prioridade, status, data_referencia, numero, senha, guiche,
   observacao, prescricao, encaminhamento,
   data_hora_chegada, data_hora_chamada, data_hora_inicio, data_hora_fim)
SELECT p.id, f.id,
       CASE WHEN v.status = 'AGUARDANDO' THEN NULL ELSE (SELECT min(id) FROM usuarios) END,
       v.prioridade, v.status, CURRENT_DATE, v.numero, f.sigla || lpad(v.numero::text, 3, '0'), v.guiche,
       v.obs, v.presc, v.enc,
       now() - make_interval(mins => v.m_chegada),
       now() - make_interval(mins => v.m_chamada),
       now() - make_interval(mins => v.m_inicio),
       now() - make_interval(mins => v.m_fim)
FROM (VALUES
  -- sigla, cpf, prioridade, status, numero, guiche, obs, presc, enc, chegada, chamada, inicio, fim
  ('T', '11111111101', 'SUPERPREFERENCIAL', 'FINALIZADO',     1, 'Guichê 1',
     'Pressão arterial 13x8. Encaminhada ao clínico.', NULL, 'Encaminhar ao Clínico Geral',
     150, 140, 138, 125),
  ('T', '11111111104', 'NORMAL',            'FINALIZADO',     2, 'Guichê 1',
     'Queixa de dor de garganta há 3 dias.', NULL, 'Encaminhar ao Clínico Geral',
     140, 120, 118, 105),
  ('T', '11111111105', 'GESTANTE',          'CANCELADO',      3, NULL,
     NULL, NULL, NULL,
     100, NULL, NULL, 90),
  ('T', '11111111108', 'NORMAL',            'AGUARDANDO',     4, NULL,
     NULL, NULL, NULL,
     35, NULL, NULL, NULL),
  ('T', '11111111111', 'PCD',               'AGUARDANDO',     5, NULL,
     NULL, NULL, NULL,
     20, NULL, NULL, NULL),
  ('C', '11111111101', 'SUPERPREFERENCIAL', 'FINALIZADO',     1, 'Consultório 2',
     'Hipertensão controlada. Manter medicação atual.',
     'Losartana 50 mg, 1 comprimido ao dia, uso contínuo.', 'Agendar retorno em 30 dias',
     120, 100, 98, 80),
  ('C', '11111111104', 'NORMAL',            'EM_ATENDIMENTO', 2, 'Consultório 1',
     NULL, NULL, NULL,
     90, 25, 20, NULL),
  ('C', '11111111102', 'PREFERENCIAL',      'CHAMADO',        3, 'Consultório 2',
     NULL, NULL, NULL,
     70, 3, NULL, NULL),
  ('C', '11111111109', 'NORMAL',            'AGUARDANDO',     4, NULL,
     NULL, NULL, NULL,
     45, NULL, NULL, NULL),
  ('C', '11111111110', 'PREFERENCIAL',      'AGUARDANDO',     5, NULL,
     NULL, NULL, NULL,
     30, NULL, NULL, NULL),
  ('C', '11111111112', 'NORMAL',            'NAO_COMPARECEU', 6, 'Consultório 1',
     NULL, NULL, NULL,
     110, 95, NULL, 90),
  ('P', '11111111106', 'NORMAL',            'EM_ATENDIMENTO', 1, 'Consultório 3',
     NULL, NULL, NULL,
     60, 18, 15, NULL),
  ('P', '11111111107', 'NORMAL',            'AGUARDANDO',     2, NULL,
     NULL, NULL, NULL,
     25, NULL, NULL, NULL),
  ('E', '11111111103', 'NORMAL',            'AGUARDANDO',     1, NULL,
     NULL, NULL, NULL,
     15, NULL, NULL, NULL)
) AS v(sigla, cpf, prioridade, status, numero, guiche, obs, presc, enc,
       m_chegada, m_chamada, m_inicio, m_fim)
JOIN filas f ON f.sigla = v.sigla
JOIN pacientes p ON p.cpf = v.cpf
ON CONFLICT DO NOTHING;

