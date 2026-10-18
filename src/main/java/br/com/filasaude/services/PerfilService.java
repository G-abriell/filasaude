package br.com.filasaude.services;

import br.com.filasaude.entities.Perfil;
import br.com.filasaude.exceptions.RecursoNaoEncontradoException;
import br.com.filasaude.exceptions.RegraNegocioException;
import br.com.filasaude.repositories.PerfilRepository;
import br.com.filasaude.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PerfilService {

    private final PerfilRepository perfilRepository;
    private final UsuarioRepository usuarioRepository;

    public PerfilService(PerfilRepository perfilRepository, UsuarioRepository usuarioRepository) {
        this.perfilRepository = perfilRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Perfil> listar() {
        return perfilRepository.findAllByOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public Perfil buscarPorId(Long id) {
        return perfilRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Perfil", id));
    }

    @Transactional
    public Perfil salvar(Perfil perfil) {
        perfil.setNome(perfil.getNome().trim().toUpperCase());
        perfilRepository.findByNomeIgnoreCase(perfil.getNome())
                .filter(existente -> !existente.getId().equals(perfil.getId()))
                .ifPresent(existente -> {
                    throw new RegraNegocioException("Já existe um perfil com o nome " + perfil.getNome() + ".");
                });
        return perfilRepository.save(perfil);
    }

    @Transactional
    public void excluir(Long id) {
        Perfil perfil = buscarPorId(id);
        if (usuarioRepository.existsByPerfilId(id)) {
            throw new RegraNegocioException("O perfil " + perfil.getNome() + " possui usuários vinculados.");
        }
        perfilRepository.delete(perfil);
    }
}
