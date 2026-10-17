package br.com.filasaude.repositories;

import br.com.filasaude.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** Usado pelo UserDetailsService quando o Spring Security for adicionado. */
    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByPerfilId(Long perfilId);

    List<Usuario> findAllByOrderByNomeAsc();

    List<Usuario> findByAtivoTrueOrderByNomeAsc();
}
