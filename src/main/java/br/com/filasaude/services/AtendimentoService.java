package br.com.filasaude.services;

import br.com.filasaude.entities.Atendimento;
import br.com.filasaude.entities.Fila;
import br.com.filasaude.entities.Paciente;
import br.com.filasaude.entities.Usuario;
import br.com.filasaude.entities.enums.Prioridade;
import br.com.filasaude.entities.enums.StatusAtendimento;
import br.com.filasaude.exceptions.RecursoNaoEncontradoException;
import br.com.filasaude.exceptions.RegraNegocioException;
import br.com.filasaude.repositories.AtendimentoRepository;
import br.com.filasaude.repositories.FilaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Service
public class AtendimentoService {

    /** Regra de intercalação 2 por 1: a cada 2 atendimentos preferenciais chamados, intercala 1 normal (se houver). */
    public static final int PROPORCAO_PREFERENCIAL_POR_NORMAL = 2;

    private final AtendimentoRepository atendimentoRepository;
    private final FilaRepository filaRepository;
    private final PacienteService pacienteService;
    private final UsuarioService usuarioService;

    public AtendimentoService(AtendimentoRepository atendimentoRepository,
                              FilaRepository filaRepository,
                              PacienteService pacienteService,
                              UsuarioService usuarioService) {
        this.atendimentoRepository = atendimentoRepository;
        this.filaRepository = filaRepository;
        this.pacienteService = pacienteService;
        this.usuarioService = usuarioService;
    }

    // ---------- Consultas ----------

    @Transactional(readOnly = true)
    public Atendimento buscarPorId(Long id) {
        return atendimentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atendimento", id));
    }

    @Transactional(readOnly = true)
    public Atendimento buscarDetalhadoPorId(Long id) {
        return atendimentoRepository.findByIdDetalhado(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atendimento", id));
    }

    @Transactional(readOnly = true)
    public List<Atendimento> listarDoDia(Long filaId) {
        return atendimentoRepository.findDoDia(LocalDate.now(), filaId);
    }

    @Transactional(readOnly = true)
    public List<Atendimento> listarAguardando(Long filaId) {
        List<Atendimento> aguardando = (filaId == null)
                ? atendimentoRepository.findAllAguardando()
                : atendimentoRepository.findAguardandoNaFila(filaId);
        int preferenciaisConsecutivos = contarPreferenciaisConsecutivos(filaId);
        return intercalarFila2Por1(aguardando, preferenciaisConsecutivos);
    }

    /**
     * Conta quantos atendimentos preferenciais consecutivos foram chamados por último hoje
     * (até o limite da proporção 2 por 1).
     */
    private int contarPreferenciaisConsecutivos(Long filaId) {
        List<Atendimento> ultimosChamados = atendimentoRepository.findUltimosChamadosDoDia(LocalDate.now(), filaId);
        int consecutivos = 0;
        for (Atendimento chamado : ultimosChamados) {
            if (chamado.getPrioridade().isLegal()) {
                consecutivos++;
                if (consecutivos >= PROPORCAO_PREFERENCIAL_POR_NORMAL) {
                    break;
                }
            } else {
                break;
            }
        }
        return consecutivos;
    }

    /**
     * Organiza a fila aplicando a regra de intercalação 2 por 1:
     * a cada 2 pacientes preferenciais (Superpreferencial 80+ e Preferencial),
     * intercala 1 paciente da fila Normal (se houver).
     */
    public static List<Atendimento> intercalarFila2Por1(List<Atendimento> aguardando, int preferenciaisConsecutivos) {
        if (aguardando == null || aguardando.size() <= 1) {
            return aguardando;
        }

        List<Atendimento> preferenciais = new ArrayList<>();
        List<Atendimento> normais = new ArrayList<>();
        for (Atendimento a : aguardando) {
            if (a.getPrioridade().isLegal()) {
                preferenciais.add(a);
            } else {
                normais.add(a);
            }
        }

        if (preferenciais.isEmpty() || normais.isEmpty()) {
            return aguardando;
        }

        List<Atendimento> resultado = new ArrayList<>(aguardando.size());
        int prefRestantesNoCiclo = Math.max(0, PROPORCAO_PREFERENCIAL_POR_NORMAL - preferenciaisConsecutivos);
        int iPref = 0;
        int iNorm = 0;

        while (iPref < preferenciais.size() || iNorm < normais.size()) {
            int chamadosNesteBloco = 0;
            while (chamadosNesteBloco < prefRestantesNoCiclo && iPref < preferenciais.size()) {
                resultado.add(preferenciais.get(iPref++));
                chamadosNesteBloco++;
            }
            if (iNorm < normais.size()) {
                resultado.add(normais.get(iNorm++));
            }
            prefRestantesNoCiclo = PROPORCAO_PREFERENCIAL_POR_NORMAL;
        }

        return resultado;
    }

    /** Atendimentos chamados ou em andamento (para o painel e os guichês). */
    @Transactional(readOnly = true)
    public List<Atendimento> listarEmAndamento() {
        return atendimentoRepository.findByStatusIn(
                EnumSet.of(StatusAtendimento.CHAMADO, StatusAtendimento.EM_ATENDIMENTO));
    }

    @Transactional(readOnly = true)
    public List<Atendimento> listarChamadosDoDia() {
        return atendimentoRepository.findChamadosDoDia(LocalDate.now());
    }

    public List<Atendimento> historicoDoPaciente(Long pacienteId) {
        return atendimentoRepository.findByPacienteIdOrderByDataHoraChegadaDesc(pacienteId);
    }

    // ---------- Operações ----------

    /**
     * Emite uma senha para o paciente na fila informada.
     * Idosos recebem prioridade automaticamente; as demais condições preferenciais
     * são informadas pela recepção ao emitir a senha.
     */
    @Transactional
    public Atendimento emitirSenha(Long pacienteId, Long filaId, Prioridade prioridade) {
        Paciente paciente = pacienteService.buscarPorId(pacienteId);
        Fila fila = filaRepository.findByIdComLock(filaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Fila", filaId));

        if (!fila.isAtiva()) {
            throw new RegraNegocioException("A fila " + fila.getNome() + " está inativa.");
        }
        if (atendimentoRepository.existsByPacienteIdAndStatusIn(pacienteId, StatusAtendimento.ATIVOS)) {
            throw new RegraNegocioException("O paciente " + paciente.getNome() + " já possui um atendimento em andamento.");
        }

        Prioridade prioridadeFinal;
        if (paciente.isSuperIdoso()) {
            prioridadeFinal = Prioridade.SUPERPREFERENCIAL;
        } else if (paciente.isIdoso()) {
            prioridadeFinal = (prioridade != null && prioridade.isLegal() && prioridade != Prioridade.SUPERPREFERENCIAL)
                    ? prioridade : Prioridade.PREFERENCIAL;
        } else if (prioridade == Prioridade.SUPERPREFERENCIAL) {
            prioridadeFinal = Prioridade.PREFERENCIAL;
        } else {
            prioridadeFinal = (prioridade != null && prioridade.isLegal())
                    ? prioridade : Prioridade.NORMAL;
        }

        LocalDateTime agora = LocalDateTime.now();
        int proximoNumero = atendimentoRepository.findUltimoNumero(filaId, agora.toLocalDate()) + 1;

        return atendimentoRepository.save(new Atendimento(paciente, fila, prioridadeFinal, proximoNumero, agora));
    }

    /**
     * Chama o próximo paciente aguardando (da fila selecionada ou de qualquer fila ativa,
     * aplicando a regra de intercalação 2 preferenciais para 1 normal e ordem de chegada).
     */
    @Transactional
    public Atendimento chamarProximo(Long filaId, Long atendenteId, String guiche) {
        Usuario atendente = validarAtendente(atendenteId);

        if (filaId != null) {
            Fila fila = filaRepository.findByIdComLock(filaId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Fila", filaId));
            if (!fila.isAtiva()) {
                throw new RegraNegocioException("A fila " + fila.getNome() + " está inativa.");
            }
        }

        List<Atendimento> candidatos = listarAguardando(filaId);
        Atendimento proximo = candidatos.stream()
                .findFirst()
                .orElseThrow(() -> new RegraNegocioException("Nenhum paciente aguardando na fila no momento."));

        if (filaId == null) {
            filaRepository.findByIdComLock(proximo.getFila().getId());
        }

        proximo.chamar(atendente, guiche);
        return proximo;
    }

    /**
     * Chama diretamente um atendimento específico que está aguardando.
     */
    @Transactional
    public Atendimento chamarPorId(Long id, Long atendenteId, String guiche) {
        Usuario atendente = validarAtendente(atendenteId);
        Atendimento atendimento = buscarDetalhadoPorId(id);
        filaRepository.findByIdComLock(atendimento.getFila().getId());
        atendimento.chamar(atendente, guiche);
        return atendimento;
    }

    /**
     * Rechama um paciente já chamado, atualizando o horário de chamada no painel.
     */
    @Transactional
    public Atendimento rechamar(Long id, Long atendenteId, String guiche) {
        Usuario atendente = validarAtendente(atendenteId);
        Atendimento atendimento = buscarDetalhadoPorId(id);
        atendimento.rechamar(atendente, guiche);
        return atendimento;
    }

    private Usuario validarAtendente(Long atendenteId) {
        Usuario atendente = usuarioService.buscarPorId(atendenteId);
        if (!atendente.isAtivo()) {
            throw new RegraNegocioException("O usuário " + atendente.getNome() + " está inativo.");
        }
        return atendente;
    }

    @Transactional
    public void iniciar(Long id) {
        buscarPorId(id).iniciar();
    }

    @Transactional
    public void finalizar(Long id, String observacao) {
        buscarPorId(id).finalizar(observacao, null, null);
    }

    @Transactional
    public void finalizar(Long id, String observacao, String prescricao, String encaminhamento) {
        buscarPorId(id).finalizar(observacao, prescricao, encaminhamento);
    }

    @Transactional
    public void registrarNaoComparecimento(Long id) {
        buscarPorId(id).registrarNaoComparecimento();
    }

    @Transactional
    public void cancelar(Long id) {
        buscarPorId(id).cancelar();
    }
}
