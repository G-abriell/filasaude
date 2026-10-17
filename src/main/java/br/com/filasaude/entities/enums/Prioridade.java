package br.com.filasaude.entities.enums;

/**
 * Substitui a antiga ideia de entidade TipoFila: o "tipo" de atendimento
 * (normal ou preferencial) é um conjunto fixo, definido em lei, e não precisa de tabela.
 */
public enum Prioridade {

    NORMAL("Normal", "N"),
    PREFERENCIAL("Preferencial", "P");

    private final String descricao;
    private final String codigo;

    Prioridade(String descricao, String codigo) {
        this.descricao = descricao;
        this.codigo = codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getCodigo() {
        return codigo;
    }
}
