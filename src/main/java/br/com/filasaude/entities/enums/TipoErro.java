package br.com.filasaude.entities.enums;

/** Só entram aqui problemas que o usuário não consegue resolver sozinho. */
public enum TipoErro {

    INESPERADO("Erro do servidor"),
    REQUISICAO_INVALIDA("Requisição inválida"),
    SEGURANCA("Alerta de segurança");

    private final String descricao;

    TipoErro(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
