package br.com.filasaude.entities;

import br.com.filasaude.entities.enums.Prioridade;
import br.com.filasaude.entities.enums.StatusAtendimento;
import br.com.filasaude.exceptions.RegraNegocioException;
import jakarta.persistence.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Representa a senha retirada por um paciente em uma fila e todo o seu ciclo de vida:
 * AGUARDANDO -> CHAMADO -> EM_ATENDIMENTO -> FINALIZADO
 * (ou NAO_COMPARECEU / CANCELADO).
 */
@Entity
@Table(name = "atendimentos",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_atendimento_fila_data_numero",
                columnNames = {"fila_id", "data_referencia", "numero"}),
        indexes = @Index(name = "idx_atendimento_fila_status", columnList = "fila_id, status"))
public class Atendimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "fila_id", nullable = false)
    private Fila fila;

    /** Usuário que chamou/atendeu o paciente. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atendente_id")
    private Usuario atendente;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridade", nullable = false, length = 30)
    private Prioridade prioridade;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private StatusAtendimento status;

    /** Dia ao qual a numeração pertence (reinicia diariamente por fila). */
    @Column(name = "data_referencia", nullable = false)
    private LocalDate dataReferencia;

    @Column(name = "numero", nullable = false)
    private Integer numero;

    /** Senha exibida no painel: uma letra e três números, sem espaço (ex.: "A001"). */
    @Column(name = "senha", nullable = false, length = 30)
    private String senha;

    @Column(name = "guiche", length = 30)
    private String guiche;

    /** Evolução clínica / resumo do atendimento registrado pelo profissional de saúde. */
    @Column(name = "observacao", length = 1000)
    private String observacao;

    /** Receita médica / prescrição emitida durante o atendimento. */
    @Column(name = "prescricao", length = 3000)
    private String prescricao;

    /** Orientação / encaminhamento direto para a Recepção (ex.: Alta liberada, Agendar retorno, Solicitar exames). */
    @Column(name = "encaminhamento", length = 500)
    private String encaminhamento;

    @Column(name = "data_hora_chegada", nullable = false)
    private LocalDateTime dataHoraChegada;

    @Column(name = "data_hora_chamada")
    private LocalDateTime dataHoraChamada;

    @Column(name = "data_hora_inicio")
    private LocalDateTime dataHoraInicio;

    @Column(name = "data_hora_fim")
    private LocalDateTime dataHoraFim;

    protected Atendimento() {
    }

    public Atendimento(Paciente paciente, Fila fila, Prioridade prioridade, int numero, LocalDateTime chegada) {
        this.paciente = paciente;
        this.fila = fila;
        this.prioridade = prioridade;
        this.numero = numero;
        this.dataHoraChegada = chegada;
        this.dataReferencia = chegada.toLocalDate();
        this.status = StatusAtendimento.AGUARDANDO;
        this.senha = String.format("%s%03d", fila.getSigla(), numero);
    }

    // ---------- Transições de estado ----------

    public void chamar(Usuario atendente, String guiche) {
        exigirStatus(StatusAtendimento.AGUARDANDO, "chamar");
        this.atendente = atendente;
        this.guiche = (guiche != null && !guiche.isBlank()) ? guiche.trim() : null;
        this.dataHoraChamada = LocalDateTime.now();
        this.status = StatusAtendimento.CHAMADO;
    }

    public void rechamar(Usuario atendente, String guiche) {
        exigirStatus(StatusAtendimento.CHAMADO, "rechamar");
        this.atendente = atendente;
        if (guiche != null && !guiche.isBlank()) {
            this.guiche = guiche.trim();
        }
        this.dataHoraChamada = LocalDateTime.now();
    }

    public void iniciar() {
        exigirStatus(StatusAtendimento.CHAMADO, "iniciar");
        this.dataHoraInicio = LocalDateTime.now();
        this.status = StatusAtendimento.EM_ATENDIMENTO;
    }

    public void finalizar(String observacao) {
        finalizar(observacao, null, null);
    }

    public void finalizar(String observacao, String prescricao, String encaminhamento) {
        exigirStatus(StatusAtendimento.EM_ATENDIMENTO, "finalizar");
        this.observacao = (observacao != null && !observacao.isBlank()) ? observacao.trim() : null;
        this.prescricao = (prescricao != null && !prescricao.isBlank()) ? prescricao.trim() : null;
        this.encaminhamento = (encaminhamento != null && !encaminhamento.isBlank()) ? encaminhamento.trim() : null;
        this.dataHoraFim = LocalDateTime.now();
        this.status = StatusAtendimento.FINALIZADO;
    }

    public boolean possuiRegistroClinico() {
        return (observacao != null && !observacao.isBlank())
                || (prescricao != null && !prescricao.isBlank())
                || (encaminhamento != null && !encaminhamento.isBlank());
    }

    public void registrarNaoComparecimento() {
        exigirStatus(StatusAtendimento.CHAMADO, "registrar não comparecimento de");
        this.dataHoraFim = LocalDateTime.now();
        this.status = StatusAtendimento.NAO_COMPARECEU;
    }

    public void cancelar() {
        if (status != StatusAtendimento.AGUARDANDO && status != StatusAtendimento.CHAMADO) {
            throw new RegraNegocioException("Não é possível cancelar um atendimento com status " + status.getDescricao() + ".");
        }
        this.dataHoraFim = LocalDateTime.now();
        this.status = StatusAtendimento.CANCELADO;
    }

    private void exigirStatus(StatusAtendimento esperado, String acao) {
        if (status != esperado) {
            throw new RegraNegocioException(String.format(
                    "Não é possível %s o atendimento %s: status atual é %s", acao, senha, status.getDescricao()) + ".");
        }
    }

    /** Minutos entre a chegada e a chamada (ou até agora, se ainda aguardando). */
    public long getMinutosEspera() {
        LocalDateTime fim = dataHoraChamada != null ? dataHoraChamada : LocalDateTime.now();
        return Duration.between(dataHoraChegada, fim).toMinutes();
    }

    // ---------- Getters ----------

    public Long getId() {
        return id;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public Fila getFila() {
        return fila;
    }

    public Usuario getAtendente() {
        return atendente;
    }

    public Prioridade getPrioridade() {
        return prioridade;
    }

    public StatusAtendimento getStatus() {
        return status;
    }

    public LocalDate getDataReferencia() {
        return dataReferencia;
    }

    public Integer getNumero() {
        return numero;
    }

    public String getSenha() {
        return senha;
    }

    public String getGuiche() {
        return guiche;
    }

    public String getObservacao() {
        return observacao;
    }

    public String getPrescricao() {
        return prescricao;
    }

    public String getEncaminhamento() {
        return encaminhamento;
    }

    public LocalDateTime getDataHoraChegada() {
        return dataHoraChegada;
    }

    public LocalDateTime getDataHoraChamada() {
        return dataHoraChamada;
    }

    public LocalDateTime getDataHoraInicio() {
        return dataHoraInicio;
    }

    public LocalDateTime getDataHoraFim() {
        return dataHoraFim;
    }
}
