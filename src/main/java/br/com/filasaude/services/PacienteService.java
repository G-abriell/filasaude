package br.com.filasaude.services;

import br.com.filasaude.entities.Paciente;
import br.com.filasaude.exceptions.RecursoNaoEncontradoException;
import br.com.filasaude.exceptions.RegraNegocioException;
import br.com.filasaude.repositories.AtendimentoRepository;
import br.com.filasaude.repositories.PacienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final AtendimentoRepository atendimentoRepository;

    public PacienteService(PacienteRepository pacienteRepository, AtendimentoRepository atendimentoRepository) {
        this.pacienteRepository = pacienteRepository;
        this.atendimentoRepository = atendimentoRepository;
    }

    @Transactional(readOnly = true)
    public List<Paciente> listar(String busca) {
        if (busca == null || busca.isBlank()) {
            return pacienteRepository.findAllByOrderByNomeAsc();
        }
        String termo = busca.trim();
        String digitos = termo.replaceAll("\\D", "");
        if (digitos.isEmpty()) {
            return pacienteRepository.findByNomeContainingIgnoreCaseOrCpfContainingOrderByNomeAsc(termo, termo);
        }
        return pacienteRepository.buscarPorTermoOuDigitos(termo, digitos);
    }

    @Transactional(readOnly = true)
    public Paciente buscarPorId(Long id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente", id));
    }

    @Transactional
    public Paciente salvar(Paciente paciente) {
        pacienteRepository.findByCpf(paciente.getCpf())
                .filter(existente -> !existente.getId().equals(paciente.getId()))
                .ifPresent(existente -> {
                    throw new RegraNegocioException("Já existe um paciente com o CPF informado.");
                });
        if (paciente.getCartaoSus() != null) {
            pacienteRepository.findByCartaoSus(paciente.getCartaoSus())
                    .filter(existente -> !existente.getId().equals(paciente.getId()))
                    .ifPresent(existente -> {
                        throw new RegraNegocioException("Já existe um paciente com o Cartão SUS informado.");
                    });
        }
        if (paciente.getId() != null) {
            // Preserva campos que não vêm do formulário
            Paciente atual = buscarPorId(paciente.getId());
            atual.setNome(paciente.getNome());
            atual.setCpf(paciente.getCpf());
            atual.setCartaoSus(paciente.getCartaoSus());
            atual.setDataNascimento(paciente.getDataNascimento());
            atual.setTelefone(paciente.getTelefone());
            return atual;
        }
        return pacienteRepository.save(paciente);
    }

    @Transactional
    public void excluir(Long id) {
        Paciente paciente = buscarPorId(id);
        if (atendimentoRepository.existsByPacienteId(id)) {
            throw new RegraNegocioException("O paciente possui histórico de atendimentos e não pode ser excluído.");
        }
        pacienteRepository.delete(paciente);
    }
}
