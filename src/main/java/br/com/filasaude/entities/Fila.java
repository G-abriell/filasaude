package br.com.filasaude.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Serviço/setor que possui uma fila própria (ex.: Triagem, Clínico Geral, Exames).
 * Os pacientes "na fila" são os atendimentos desta fila com status AGUARDANDO;
 * por isso não há uma lista de pacientes mapeada aqui.
 */
@Entity
@Table(name = "filas")
public class Fila {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "nome", nullable = false, unique = true, length = 100)
    private String nome;

    /** Letra usada na senha, ex.: "A" gera "A001" (Manual da marca: uma letra e três números). */
    @NotBlank
    @Pattern(regexp = "[A-Za-z]", message = "Use uma única letra, como A.")
    @Column(name = "sigla", nullable = false, unique = true, length = 5)
    private String sigla;

    @Size(max = 255)
    @Column(name = "descricao", length = 255)
    private String descricao;

    @Column(name = "ativa", nullable = false)
    private boolean ativa = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla == null ? null : sigla.trim().toUpperCase();
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
    }
}
