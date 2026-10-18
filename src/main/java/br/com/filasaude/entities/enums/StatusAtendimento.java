package br.com.filasaude.entities.enums;

import java.util.EnumSet;
import java.util.Set;

public enum StatusAtendimento {

    AGUARDANDO("Aguardando"),
    CHAMADO("Chamado"),
    EM_ATENDIMENTO("Em atendimento"),
    FINALIZADO("Concluído"),
    NAO_COMPARECEU("Não compareceu"),
    CANCELADO("Cancelado");

    public static final Set<StatusAtendimento> ATIVOS = EnumSet.of(AGUARDANDO, CHAMADO, EM_ATENDIMENTO);

    private final String descricao;

    StatusAtendimento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isAtivo() {
        return ATIVOS.contains(this);
    }
}
