package br.com.filasaude.repositories;

import br.com.filasaude.entities.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    Optional<Paciente> findByCpf(String cpf);

    Optional<Paciente> findByCartaoSus(String cartaoSus);

    List<Paciente> findByNomeContainingIgnoreCaseOrCpfContainingOrderByNomeAsc(String nome, String cpf);

    List<Paciente> findAllByOrderByNomeAsc();
}
