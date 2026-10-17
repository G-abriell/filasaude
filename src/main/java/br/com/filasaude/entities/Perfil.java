package br.com.filasaude.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "perfis")
public class Perfil {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "nome", length = 255)
    private String nome;
    @OneToOne(mappedBy = "perfil")
    private Usuario usuario;
}
