package br.com.filasaude.services;

import br.com.filasaude.entities.Usuario;
import br.com.filasaude.exceptions.RecursoNaoEncontradoException;
import br.com.filasaude.exceptions.RegraNegocioException;
import br.com.filasaude.repositories.AtendimentoRepository;
import br.com.filasaude.repositories.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private static final int TAMANHO_MINIMO_SENHA = 6;

    private final UsuarioRepository usuarioRepository;
    private final AtendimentoRepository atendimentoRepository;
    private final PerfilService perfilService;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          AtendimentoRepository atendimentoRepository,
                          PerfilService perfilService,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.atendimentoRepository = atendimentoRepository;
        this.perfilService = perfilService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return usuarioRepository.findAllByOrderByNomeAsc();
    }

    /** Profissionais de saúde ativos, para a recepção escolher na chamada em contingência. */
    @Transactional(readOnly = true)
    public List<Usuario> listarProfissionaisAtivos() {
        return usuarioRepository.findAtivosPorPerfil("PROFISSIONAL_DE_SAUDE");
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", id));
    }

    /**
     * Na edição, se a senha vier em branco a senha atual é mantida.
     */
    @Transactional
    public Usuario salvar(Usuario usuario) {
        usuario.setEmail(usuario.getEmail().trim().toLowerCase());
        usuarioRepository.findByEmailIgnoreCase(usuario.getEmail())
                .filter(existente -> !existente.getId().equals(usuario.getId()))
                .ifPresent(existente -> {
                    throw new RegraNegocioException("Já existe um usuário com o e-mail " + usuario.getEmail() + ".");
                });

        if (usuario.getPerfil() == null || usuario.getPerfil().getId() == null) {
            throw new RegraNegocioException("Selecione um perfil.");
        }
        usuario.setPerfil(perfilService.buscarPorId(usuario.getPerfil().getId()));

        Usuario atual = usuario.getId() == null ? null : buscarPorId(usuario.getId());
        if (atual != null && atual.isAtivo()) {
            if (!usuario.isAtivo()) {
                exigirQueNaoSejaALogada(atual, "desativar");
                exigirOutroAdministradorAtivo(atual, "desativar");
            } else if (ehAdmin(atual) && !ehAdmin(usuario)) {
                exigirOutroAdministradorAtivo(atual, "rebaixar");
            }
        }

        boolean senhaInformada = usuario.getSenha() != null && !usuario.getSenha().isBlank();
        if (atual == null && !senhaInformada) {
            throw new RegraNegocioException("A senha é obrigatória no cadastro.");
        }
        if (senhaInformada) {
            if (usuario.getSenha().length() < TAMANHO_MINIMO_SENHA) {
                throw new RegraNegocioException("A senha deve ter ao menos " + TAMANHO_MINIMO_SENHA + " caracteres.");
            }
            usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        } else {
            usuario.setSenha(atual.getSenha());
        }
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void alternarAtivo(Long id) {
        Usuario usuario = buscarPorId(id);
        if (!usuario.isAtivo()) {
            usuario.setAtivo(true);
            return;
        }
        exigirQueNaoSejaALogada(usuario, "desativar");
        exigirOutroAdministradorAtivo(usuario, "desativar");
        usuario.setAtivo(false);
    }

    @Transactional
    public void excluir(Long id) {
        Usuario usuario = buscarPorId(id);
        exigirQueNaoSejaALogada(usuario, "excluir");
        exigirOutroAdministradorAtivo(usuario, "excluir");
        if (atendimentoRepository.existsByAtendenteId(id)) {
            throw new RegraNegocioException("Este usuário tem atendimentos registrados. Desative o usuário em vez de excluir.");
        }
        usuarioRepository.delete(usuario);
    }

    /** Ninguém pode desativar ou excluir a conta com a qual está logado. */
    private void exigirQueNaoSejaALogada(Usuario usuario, String acao) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailLogado = authentication == null ? null
                : authentication.getPrincipal() instanceof UserDetails userDetails
                ? userDetails.getUsername() : authentication.getName();
        if (emailLogado != null && emailLogado.equalsIgnoreCase(usuario.getEmail())) {
            throw new RegraNegocioException("Não é possível " + acao + " a própria conta.");
        }
    }

    /** O sistema precisa manter ao menos um administrador ativo; a consulta trava as linhas para evitar corrida. */
    private void exigirOutroAdministradorAtivo(Usuario usuario, String acao) {
        if (usuario.isAtivo() && ehAdmin(usuario)
                && usuarioRepository.findAdministradoresAtivosComLock().size() <= 1) {
            throw new RegraNegocioException("Não é possível " + acao + " o último administrador ativo.");
        }
    }

    private boolean ehAdmin(Usuario usuario) {
        return usuario.getPerfil() != null && "ADMIN".equalsIgnoreCase(usuario.getPerfil().getNome());
    }
}
