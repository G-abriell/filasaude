package br.com.filasaude.entities;

import br.com.filasaude.entities.enums.TipoErro;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Problema que só o desenvolvedor ou a administração podem tratar (erro do servidor, requisição
 * adulterada, alerta de segurança). Ocorrências iguais em sequência são agrupadas na mesma linha.
 * Não guarda o conteúdo dos formulários: só a mensagem, o local e quem estava logado.
 */
@Entity
@Table(name = "registros_erro",
        indexes = {
                @Index(name = "idx_registro_erro_ultima", columnList = "ultima_ocorrencia"),
                @Index(name = "idx_registro_erro_chave", columnList = "chave")})
public class RegistroErro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Primeira ocorrência do grupo. */
    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(name = "ultima_ocorrencia")
    private LocalDateTime ultimaOcorrencia;

    /** Quantas vezes o mesmo problema ocorreu dentro da janela de agrupamento. */
    @Column(name = "ocorrencias", nullable = false, columnDefinition = "integer default 1")
    private int ocorrencias = 1;

    /** Identifica problemas iguais (tipo + causa + local sem ids) para agrupá-los. */
    @Column(name = "chave", length = 300)
    private String chave;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoErro tipo;

    @Column(name = "mensagem", nullable = false, length = 500)
    private String mensagem;

    /** Causa técnica (pilha resumida), preenchida nos erros do servidor. */
    @Column(name = "detalhe", length = 4000)
    private String detalhe;

    @Column(name = "usuario_email", length = 100)
    private String usuarioEmail;

    @Column(name = "metodo", length = 10)
    private String metodo;

    @Column(name = "caminho", length = 255)
    private String caminho;

    protected RegistroErro() {
    }

    public RegistroErro(TipoErro tipo, String chave, String mensagem, String detalhe, String usuarioEmail,
                        String metodo, String caminho) {
        this.dataHora = LocalDateTime.now();
        this.ultimaOcorrencia = this.dataHora;
        this.tipo = tipo;
        this.chave = chave;
        this.mensagem = mensagem;
        this.detalhe = detalhe;
        this.usuarioEmail = usuarioEmail;
        this.metodo = metodo;
        this.caminho = caminho;
    }

    public void registrarNovaOcorrencia() {
        this.ocorrencias++;
        this.ultimaOcorrencia = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public LocalDateTime getUltimaOcorrencia() {
        return ultimaOcorrencia;
    }

    public int getOcorrencias() {
        return ocorrencias;
    }

    public String getChave() {
        return chave;
    }

    public TipoErro getTipo() {
        return tipo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public String getDetalhe() {
        return detalhe;
    }

    public String getUsuarioEmail() {
        return usuarioEmail;
    }

    public String getMetodo() {
        return metodo;
    }

    public String getCaminho() {
        return caminho;
    }
}
