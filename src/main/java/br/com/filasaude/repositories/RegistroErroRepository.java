package br.com.filasaude.repositories;

import br.com.filasaude.entities.RegistroErro;
import br.com.filasaude.entities.enums.TipoErro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RegistroErroRepository extends JpaRepository<RegistroErro, Long> {

    Page<RegistroErro> findAllByOrderByUltimaOcorrenciaDescIdDesc(Pageable pageable);

    Page<RegistroErro> findByTipoOrderByUltimaOcorrenciaDescIdDesc(TipoErro tipo, Pageable pageable);

    /** Grupo ainda "aberto" do mesmo problema: ocorreu de novo dentro da janela. */
    Optional<RegistroErro> findFirstByChaveAndUltimaOcorrenciaAfterOrderByUltimaOcorrenciaDesc(
            String chave, LocalDateTime desde);

    long countByUltimaOcorrenciaAfter(LocalDateTime desde);
}
