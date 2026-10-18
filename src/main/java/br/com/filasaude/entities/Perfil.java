package br.com.filasaude.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Perfil de acesso (ex.: ADMIN, RECEPCAO, PROFISSIONAL_DE_SAUDE).
 * Será usado como "role" pelo Spring Security.
 */
@Entity
@Table(name = "perfis")
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "nome", nullable = false, unique = true, length = 50)
    private String nome;

    @Size(max = 255)
    @Column(name = "descricao", length = 255)
    private String descricao;

    public Perfil() {
    }

    public Perfil(Long id, String nome, String descricao) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
    }

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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
