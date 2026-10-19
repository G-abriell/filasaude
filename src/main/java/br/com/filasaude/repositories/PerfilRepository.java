package br.com.filasaude.repositories;

import br.com.filasaude.entities.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerfilRepository extends JpaRepository<Perfil, Long> {

    Optional<Perfil> findByNomeIgnoreCase(String nome);

    List<Perfil> findAllByOrderByNomeAsc();
}
