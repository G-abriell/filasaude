package br.com.filasaude.entities.enums;

/**
 * Hierarquia de prioridade legal no atendimento à saúde conforme legislação brasileira:
 * - Lei Federal nº 10.048/2000 (Prioridade Geral: 60+, PCD, Gestantes, Lactantes, TEA)
 * - Lei Federal nº 10.741/2003 (Estatuto da Pessoa Idosa)
 * - Lei Federal nº 13.466/2017 (Superprioridade para maiores de 80 anos sobre os demais)
 */
public enum Prioridade {

    NORMAL("Normal", 3),
    PREFERENCIAL("Preferencial (Lei 10.048/2000)", 2),
    GESTANTE("Preferencial - gestante", 2),
    OBESIDADE("Preferencial - pessoa com obesidade", 2),
    PCD("Preferencial - pessoa com deficiência (PCD)", 2),
    TEA("Preferencial - pessoa autista (TEA)", 2),
    MULHER_COM_CRIANCA_DE_COLO("Preferencial - mulher com criança de colo", 2),
    SUPERPREFERENCIAL("Superpreferencial 80+ (Lei 13.466/2017)", 1);

    private final String descricao;
    private final int ordem;

    Prioridade(String descricao, int ordem) {
        this.descricao = descricao;
        this.ordem = ordem;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getOrdem() {
        return ordem;
    }

    public boolean isLegal() {
        return this != NORMAL;
    }
}
