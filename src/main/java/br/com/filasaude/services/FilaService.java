package br.com.filasaude.services;

import br.com.filasaude.entities.Fila;
import br.com.filasaude.exceptions.RecursoNaoEncontradoException;
import br.com.filasaude.exceptions.RegraNegocioException;
import br.com.filasaude.repositories.AtendimentoRepository;
import br.com.filasaude.repositories.FilaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FilaService {

    private final FilaRepository filaRepository;
    private final AtendimentoRepository atendimentoRepository;

    public FilaService(FilaRepository filaRepository, AtendimentoRepository atendimentoRepository) {
        this.filaRepository = filaRepository;
        this.atendimentoRepository = atendimentoRepository;
    }

    @Transactional(readOnly = true)
    public List<Fila> listar() {
        return filaRepository.findAllByOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public List<Fila> listarAtivas() {
        return filaRepository.findByAtivaTrueOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public Fila buscarPorId(Long id) {
        return filaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Fila", id));
    }

    @Transactional
    public Fila salvar(Fila fila) {
        fila.setNome(fila.getNome().trim());
        filaRepository.findByNomeIgnoreCase(fila.getNome())
                .filter(existente -> !existente.getId().equals(fila.getId()))
                .ifPresent(existente -> {
                    throw new RegraNegocioException("Já existe uma fila com o nome " + fila.getNome());
                });
        filaRepository.findBySiglaIgnoreCase(fila.getSigla())
                .filter(existente -> !existente.getId().equals(fila.getId()))
                .ifPresent(existente -> {
                    throw new RegraNegocioException("A sigla " + fila.getSigla() + " já está em uso pela fila " + existente.getNome());
                });
        return filaRepository.save(fila);
    }

    @Transactional
    public void alternarAtiva(Long id) {
        Fila fila = buscarPorId(id);
        fila.setAtiva(!fila.isAtiva());
    }

    @Transactional
    public void excluir(Long id) {
        Fila fila = buscarPorId(id);
        if (atendimentoRepository.existsByFilaId(id)) {
            throw new RegraNegocioException("A fila possui atendimentos registrados; desative-a em vez de excluir");
        }
        filaRepository.delete(fila);
    }
}
