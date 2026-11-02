package br.com.filasaude.services;

import br.com.filasaude.entities.RegistroErro;
import br.com.filasaude.entities.enums.TipoErro;
import br.com.filasaude.repositories.RegistroErroRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Guarda para o administrador os problemas que o usuário NÃO consegue resolver sozinho:
 * erros do servidor, requisições adulteradas ou malformadas e alertas de segurança.
 * Erros de uso (CPF duplicado, campo inválido, registro não encontrado) não passam por aqui.
 *
 * Ocorrências iguais dentro de {@link #JANELA_DE_AGRUPAMENTO} são somadas na mesma linha.
 * Registrar nunca pode causar outro erro: qualquer falha ao gravar só é escrita no log do servidor.
 * O conteúdo digitado nos formulários não é guardado.
 */
@Service
public class RegistroErroService {

    public static final int TAMANHO_PAGINA = 50;
    public static final Duration JANELA_DE_AGRUPAMENTO = Duration.ofMinutes(30);

    private static final int MAX_MENSAGEM = 500;
    private static final int MAX_DETALHE = 4000;
    private static final int MAX_CHAVE = 300;

    private static final Logger log = LoggerFactory.getLogger(RegistroErroService.class);

    private final RegistroErroRepository repository;

    public RegistroErroService(RegistroErroRepository repository) {
        this.repository = repository;
    }

    // ---------- Gravação ----------

    /** Erro do servidor que ninguém tratou (HTTP 5xx). */
    public void inesperado(Throwable erro, String metodo, String caminho) {
        Throwable raiz = raiz(erro);
        String mensagem = erro == null ? "Erro inesperado no servidor."
                : "Erro inesperado: " + raiz.getClass().getSimpleName()
                + (raiz.getMessage() == null ? "" : " - " + raiz.getMessage());
        log.error("Erro inesperado em {} {}", metodo, caminho, erro);
        String chave = "INESPERADO|" + (erro == null ? "?" : raiz.getClass().getName()) + "|" + metodo + " " + normalizar(caminho);
        registrar(TipoErro.INESPERADO, chave, mensagem, erro == null ? null : pilha(erro), metodo, caminho);
    }

    /** Requisição que a interface nunca gera (parâmetro adulterado, método errado): ataque ou defeito no front. */
    public void requisicaoInvalida(int status, Throwable erro, String metodo, String caminho) {
        String causa = erro == null ? "" : ": " + raiz(erro).getClass().getSimpleName()
                + (raiz(erro).getMessage() == null ? "" : " - " + raiz(erro).getMessage());
        String mensagem = "Requisição inválida (HTTP " + status + ")" + causa;
        log.warn("{} em {} {}", mensagem, metodo, caminho);
        String chave = "REQUISICAO|" + status + "|" + metodo + " " + normalizar(caminho);
        registrar(TipoErro.REQUISICAO_INVALIDA, chave, mensagem, null, metodo, caminho);
    }

    /** Alerta de segurança. {@code chaveBase} identifica o mesmo evento (ex.: usuário + tipo) para agrupar. */
    public void seguranca(String mensagem, String chaveBase, String metodo, String caminho) {
        log.warn("ALERTA DE SEGURANÇA: {} ({} {})", mensagem, metodo, caminho);
        String chave = "SEGURANCA|" + chaveBase + "|" + metodo + " " + normalizar(caminho);
        registrar(TipoErro.SEGURANCA, chave, mensagem, null, metodo, caminho);
    }

    private void registrar(TipoErro tipo, String chave, String mensagem, String detalhe, String metodo, String caminho) {
        try {
            String chaveCortada = cortar(chave, MAX_CHAVE);
            var existente = repository.findFirstByChaveAndUltimaOcorrenciaAfterOrderByUltimaOcorrenciaDesc(
                    chaveCortada, LocalDateTime.now().minus(JANELA_DE_AGRUPAMENTO));
            if (existente.isPresent()) {
                RegistroErro grupo = existente.get();
                grupo.registrarNovaOcorrencia();
                repository.save(grupo);
                return;
            }
            repository.save(new RegistroErro(tipo, chaveCortada, cortar(mensagem, MAX_MENSAGEM),
                    cortar(detalhe, MAX_DETALHE), usuarioLogado(), cortar(metodo, 10), cortar(caminho, 255)));
        } catch (RuntimeException e) {
            log.error("Não foi possível guardar o registro de erro: {}", e.getMessage());
        }
    }

    // ---------- Consulta (administrador) ----------

    @Transactional(readOnly = true)
    public Page<RegistroErro> listar(TipoErro tipo, int pagina) {
        PageRequest pageable = PageRequest.of(Math.max(0, pagina), TAMANHO_PAGINA);
        return tipo == null
                ? repository.findAllByOrderByUltimaOcorrenciaDescIdDesc(pageable)
                : repository.findByTipoOrderByUltimaOcorrenciaDescIdDesc(tipo, pageable);
    }

    @Transactional(readOnly = true)
    public long contarUltimas24Horas() {
        return repository.countByUltimaOcorrenciaAfter(LocalDateTime.now().minusHours(24));
    }

    // ---------- Apoio ----------

    /** Troca ids numéricos do caminho por {id} e remove a query: /pacientes/12/excluir -> /pacientes/{id}/excluir. */
    static String normalizar(String caminho) {
        if (caminho == null) {
            return "";
        }
        int query = caminho.indexOf('?');
        String semQuery = query >= 0 ? caminho.substring(0, query) : caminho;
        return semQuery.replaceAll("/\\d+(?=/|$)", "/{id}");
    }

    private static String usuarioLogado() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao == null || autenticacao instanceof AnonymousAuthenticationToken || !autenticacao.isAuthenticated()) {
            return null;
        }
        return cortar(autenticacao.getName(), 100);
    }

    private static Throwable raiz(Throwable erro) {
        Throwable atual = erro;
        while (atual != null && atual.getCause() != null && atual.getCause() != atual) {
            atual = atual.getCause();
        }
        return atual == null ? new IllegalStateException("desconhecido") : atual;
    }

    private static String pilha(Throwable erro) {
        StringWriter texto = new StringWriter();
        erro.printStackTrace(new PrintWriter(texto));
        return texto.toString();
    }

    private static String cortar(String texto, int limite) {
        if (texto == null || texto.length() <= limite) {
            return texto;
        }
        return texto.substring(0, limite - 1) + "…";
    }
}
