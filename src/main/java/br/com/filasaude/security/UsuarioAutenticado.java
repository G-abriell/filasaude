package br.com.filasaude.security;

import br.com.filasaude.entities.Usuario;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

/**
 * Principal do Spring Security: guarda o id e o nome do usuário para uso nos controllers e no menu.
 * O nome do perfil vira a authority ROLE_<NOME>.
 */
public class UsuarioAutenticado extends User {

    private final Long id;
    private final String nome;

    public UsuarioAutenticado(Usuario usuario) {
        super(usuario.getEmail(), usuario.getSenha(), usuario.isAtivo(), true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getPerfil().getNome().trim().toUpperCase())));
        this.id = usuario.getId();
        this.nome = usuario.getNome();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }
}
