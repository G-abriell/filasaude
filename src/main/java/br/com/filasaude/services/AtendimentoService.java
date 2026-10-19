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
    public List<Atendimento> listarDoDia(Long filaId) {
        return atendimentoRepository.findDoDia(LocalDate.now(), filaId);
    }

    @Transactional(readOnly = true)
    public List<Atendimento> listarAguardando(Long filaId) {
        return atendimentoRepository.findAguardandoNaFila(filaId);
    }

    /** Atendimentos chamados ou em andamento (para o painel e os guichês). */
    @Transactional(readOnly = true)
    public List<Atendimento> listarEmAndamento() {
        return atendimentoRepository.findByStatusIn(
                EnumSet.of(StatusAtendimento.CHAMADO, StatusAtendimento.EM_ATENDIMENTO));
    }

    @Transactional(readOnly = true)
    public List<Atendimento> historicoDoPaciente(Long pacienteId) {
        return atendimentoRepository.findByPacienteIdOrderByDataHoraChegadaDesc(pacienteId);
    }

    // ---------- Operações ----------

    /**
     * Emite uma senha para o paciente na fila informada.
     * Pacientes idosos recebem prioridade preferencial automaticamente.
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

        Prioridade prioridadeFinal = paciente.isIdoso() ? Prioridade.PREFERENCIAL
                : (prioridade != null ? prioridade : Prioridade.NORMAL);

        LocalDateTime agora = LocalDateTime.now();
        int proximoNumero = atendimentoRepository.findUltimoNumero(filaId, agora.toLocalDate()) + 1;

        return atendimentoRepository.save(new Atendimento(paciente, fila, prioridadeFinal, proximoNumero, agora));
    }

    /**
     * Chama o próximo paciente da fila (preferenciais primeiro, depois por ordem de chegada).
     */
    @Transactional
    public Atendimento chamarProximo(Long filaId, Long atendenteId, String guiche) {
        filaRepository.findByIdComLock(filaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Fila", filaId));
        Usuario atendente = usuarioService.buscarPorId(atendenteId);
        if (!atendente.isAtivo()) {
            throw new RegraNegocioException("O usuário " + atendente.getNome() + " está inativo.");
        }

        Atendimento proximo = atendimentoRepository.findAguardandoNaFila(filaId).stream()
                .findFirst()
                .orElseThrow(() -> new RegraNegocioException("Ninguém aguardando agora."));

        proximo.chamar(atendente, guiche);
        return proximo;
    }

    @Transactional
    public void iniciar(Long id) {
        buscarPorId(id).iniciar();
    }

    @Transactional
    public void finalizar(Long id, String observacao) {
        buscarPorId(id).finalizar(observacao);
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
