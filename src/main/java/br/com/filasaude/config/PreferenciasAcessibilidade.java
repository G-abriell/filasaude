package br.com.filasaude.config;

import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.util.Set;

/**
 * Preferências de acessibilidade padrão, compartilhadas pelas telas /acessibilidade e /configuracoes
 * para que ambas mostrem sempre os mesmos valores. Cada navegador guarda a própria escolha no
 * localStorage (filasaude.js); estes valores são apenas o ponto de partida enquanto o navegador não tiver escolha.
 * Ficam em memória: voltam ao padrão quando a aplicação reinicia.
 */
@Component
public class PreferenciasAcessibilidade {

    private static final Set<String> TAMANHOS_DE_FONTE = Set.of("padrao", "medio", "grande");

    private record Valores(String tamanhoFonte, boolean altoContraste, boolean focoReforcado,
                           boolean reducaoMovimento, boolean legibilidadeAprimorada) {
    }

    private volatile Valores atuais = new Valores("padrao", false, true, false, false);

    public void atualizar(String tamanhoFonte, boolean altoContraste, boolean focoReforcado,
                          boolean reducaoMovimento, boolean legibilidadeAprimorada) {
        String fonte = TAMANHOS_DE_FONTE.contains(tamanhoFonte) ? tamanhoFonte : "padrao";
        this.atuais = new Valores(fonte, altoContraste, focoReforcado, reducaoMovimento, legibilidadeAprimorada);
    }

    public void adicionarAo(Model model) {
        Valores v = atuais;
        model.addAttribute("tamanhoFonte", v.tamanhoFonte());
        model.addAttribute("altoContraste", v.altoContraste());
        model.addAttribute("focoReforcado", v.focoReforcado());
        model.addAttribute("reducaoMovimento", v.reducaoMovimento());
        model.addAttribute("legibilidadeAprimorada", v.legibilidadeAprimorada());
    }
}
