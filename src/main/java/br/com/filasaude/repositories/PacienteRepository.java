package br.com.filasaude.repositories;

import br.com.filasaude.entities.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    Optional<Paciente> findByCpf(String cpf);

    Optional<Paciente> findByCartaoSus(String cartaoSus);

    List<Paciente> findByNomeContainingIgnoreCaseOrCpfContainingOrderByNomeAsc(String nome, String cpf);

    @Query("""
            select p from Paciente p
            where lower(p.nome) like lower(concat('%', :termo, '%'))
               or p.cpf like concat('%', :digitos, '%')
               or (p.cartaoSus is not null and p.cartaoSus like concat('%', :digitos, '%'))
               or (p.telefone is not null and lower(p.telefone) like lower(concat('%', :termo, '%')))
            order by p.nome asc
            """)
    List<Paciente> buscarPorTermoOuDigitos(String termo, String digitos);

    List<Paciente> findAllByOrderByNomeAsc();
}
