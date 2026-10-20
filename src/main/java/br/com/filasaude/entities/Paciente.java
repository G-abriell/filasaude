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
    public static final int IDADE_SUPER_IDOSO = 80;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(name = "nome", nullable = false, length = 255)
    private String nome;

    @NotBlank
    @Pattern(regexp = "\\d{11}", message = "Informe os 11 números do CPF.")
    @Column(name = "cpf", nullable = false, unique = true, length = 11)
    private String cpf;

    @Pattern(regexp = "^$|\\d{15}", message = "Informe os 15 números do Cartão SUS.")
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

    /** Telefone formatado no padrão (00) 9 8888-8888 ou (00) 8888-8888. */
    public String getTelefoneFormatado() {
        if (telefone == null || telefone.isBlank()) {
            return telefone;
        }
        String digitos = telefone.replaceAll("\\D", "");
        if (digitos.length() == 11) {
            return "(" + digitos.substring(0, 2) + ") " + digitos.substring(2, 3) + " "
                    + digitos.substring(3, 7) + "-" + digitos.substring(7);
        }
        if (digitos.length() == 10) {
            return "(" + digitos.substring(0, 2) + ") " + digitos.substring(2, 6) + "-" + digitos.substring(6);
        }
        return telefone;
    }

    /** Idosos com 80 anos ou mais têm superprioridade sobre os demais idosos (Lei 13.466/2017). */
    public boolean isSuperIdoso() {
        return getIdade() >= IDADE_SUPER_IDOSO;
    }

    /** Idosos a partir de 60 anos têm direito a atendimento preferencial (Lei 10.048/2000 e Estatuto da Pessoa Idosa). */
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
        this.cpf = cpf == null ? null : cpf.replaceAll("\\D", "");
    }

    public String getCartaoSus() {
        return cartaoSus;
    }

    public void setCartaoSus(String cartaoSus) {
        if (cartaoSus == null) {
            this.cartaoSus = null;
            return;
        }
        String limpo = cartaoSus.replaceAll("\\D", "");
        this.cartaoSus = limpo.isEmpty() ? null : limpo;
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
        if (telefone == null || telefone.isBlank()) {
            this.telefone = null;
            return;
        }
        String digitos = telefone.replaceAll("\\D", "");
        if (digitos.length() == 11) {
            this.telefone = "(" + digitos.substring(0, 2) + ") " + digitos.substring(2, 3) + " "
                    + digitos.substring(3, 7) + "-" + digitos.substring(7);
        } else if (digitos.length() == 10) {
            this.telefone = "(" + digitos.substring(0, 2) + ") " + digitos.substring(2, 6) + "-" + digitos.substring(6);
        } else {
            this.telefone = telefone.trim();
        }
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }
}
