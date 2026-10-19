package br.com.filasaude.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

@Entity
@Table(name = "pacientes")
public class Paciente {

    public static final int IDADE_IDOSO = 60;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(name = "nome", nullable = false, length = 255)
    private String nome;

    @NotBlank
    @Pattern(regexp = "\\d{11}", message = "Informe os 11 números do CPF, sem pontos ou traço.")
    @Column(name = "cpf", nullable = false, unique = true, length = 11)
    private String cpf;

    @Pattern(regexp = "^$|\\d{15}", message = "Informe os 15 números do Cartão SUS, sem espaços.")
    @Column(name = "cartao_sus", unique = true, length = 15)
    private String cartaoSus;

    @NotNull
    @Past
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Size(max = 20)
    @Column(name = "telefone", length = 20)
    private String telefone;

    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @PrePersist
    void prePersist() {
        if (dataCadastro == null) {
            dataCadastro = LocalDateTime.now();
        }
    }

    public int getIdade() {
        return dataNascimento == null ? 0 : Period.between(dataNascimento, LocalDate.now()).getYears();
    }

    /** CPF no formato 000.000.000-00, para exibição. */
    public String getCpfFormatado() {
        if (cpf == null || cpf.length() != 11) {
            return cpf;
        }
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }

    /** Idosos têm direito a atendimento preferencial (Lei 10.048/2000). */
    public boolean isIdoso() {
        return getIdade() >= IDADE_IDOSO;
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

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getCartaoSus() {
        return cartaoSus;
    }

    public void setCartaoSus(String cartaoSus) {
        // Vazio vira null para não violar a constraint unique
        this.cartaoSus = (cartaoSus == null || cartaoSus.isBlank()) ? null : cartaoSus.trim();
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }
}
