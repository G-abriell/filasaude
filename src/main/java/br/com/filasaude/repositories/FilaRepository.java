package br.com.filasaude.repositories;

import br.com.filasaude.entities.Fila;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FilaRepository extends JpaRepository<Fila, Long> {

    Optional<Fila> findByNomeIgnoreCase(String nome);

    Optional<Fila> findBySiglaIgnoreCase(String sigla);

    List<Fila> findAllByOrderByNomeAsc();

    List<Fila> findByAtivaTrueOrderByNomeAsc();

    /**
     * Bloqueia a linha da fila até o fim da transação. Serializa a emissão de senhas
     * e a chamada do próximo paciente, evitando números duplicados ou dois guichês
     * chamando o mesmo paciente.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from Fila f where f.id = :id")
    Optional<Fila> findByIdComLock(Long id);
}
