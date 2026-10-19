package br.com.filasaude.entities.enums;

/**
 * Substitui a antiga ideia de entidade TipoFila: o "tipo" de atendimento
 * (normal ou preferencial) é um conjunto fixo, definido em lei, e não precisa de tabela.
 */
public enum Prioridade {

    NORMAL("Normal"),
    PREFERENCIAL("Preferencial");

    private final String descricao;

    Prioridade(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
