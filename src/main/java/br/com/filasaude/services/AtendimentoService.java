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
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Service
public class AtendimentoService {

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
        if (filaId == null) {
            return atendimentoRepository.findAllAguardando();
        }
        return atendimentoRepository.findAguardandoNaFila(filaId);
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
            prioridadeFinal = Prioridade.PREFERENCIAL;
        } else if (prioridade == Prioridade.SUPERPREFERENCIAL) {
            prioridadeFinal = Prioridade.NORMAL;
        } else if (prioridade == Prioridade.PREFERENCIAL) {
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
     * respeitando prioridade preferencial primeiro e depois ordem de chegada).
     */
    @Transactional
    public Atendimento chamarProximo(Long filaId, Long atendenteId, String guiche) {
        Usuario atendente = validarAtendente(atendenteId);

        Optional<Atendimento> candidato;
        if (filaId != null) {
            Fila fila = filaRepository.findById(filaId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Fila", filaId));
            if (!fila.isAtiva()) {
                throw new RegraNegocioException("A fila " + fila.getNome() + " está inativa.");
            }
            candidato = atendimentoRepository.chamarProximoNaFila(filaId);
        } else {
            candidato = atendimentoRepository.chamarProximoGeral();
        }

        Atendimento proximo = candidato
                .orElseThrow(() -> new RegraNegocioException("Nenhum paciente aguardando na fila no momento."));

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
