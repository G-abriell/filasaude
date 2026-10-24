package br.com.filasaude.repositories;

import br.com.filasaude.entities.Atendimento;
import br.com.filasaude.entities.enums.StatusAtendimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AtendimentoRepository extends JpaRepository<Atendimento, Long> {

    @Query("""
            select a from Atendimento a
            join fetch a.paciente
            join fetch a.fila
            left join fetch a.atendente
            where a.id = :id
            """)
    Optional<Atendimento> findByIdDetalhado(Long id);

    @Query("""
            select coalesce(max(a.numero), 0) from Atendimento a
            where a.fila.id = :filaId and a.dataReferencia = :data
            """)
    int findUltimoNumero(Long filaId, LocalDate data);

    /** Pacientes aguardando na fila: preferenciais primeiro, depois por ordem de chegada. */
    @Query("""
            select a from Atendimento a
            join fetch a.paciente
            join fetch a.fila
            where a.fila.id = :filaId and a.status = br.com.filasaude.entities.enums.StatusAtendimento.AGUARDANDO
            order by case
                        when a.prioridade = br.com.filasaude.entities.enums.Prioridade.SUPERPREFERENCIAL then 0
                        when a.prioridade = br.com.filasaude.entities.enums.Prioridade.PREFERENCIAL then 1
                        else 2
                     end,
                     a.dataHoraChegada asc
            """)
    List<Atendimento> findAguardandoNaFila(Long filaId);

    @Query(value = """
            select a.* from atendimentos a
            where a.fila_id = :filaId and a.status = 'AGUARDANDO'
            order by case a.prioridade
                        when 'SUPERPREFERENCIAL' then 0
                        when 'PREFERENCIAL' then 1
                        else 2
                     end, a.data_hora_chegada asc
            limit 1
            for update skip locked
            """, nativeQuery = true)
    Optional<Atendimento> chamarProximoNaFila(Long filaId);

    /** Todos os pacientes aguardando em filas ativas: preferenciais primeiro, depois por ordem de chegada. */
    @Query("""
            select a from Atendimento a
            join fetch a.paciente
            join fetch a.fila
            where a.fila.ativa = true and a.status = br.com.filasaude.entities.enums.StatusAtendimento.AGUARDANDO
            order by case
                        when a.prioridade = br.com.filasaude.entities.enums.Prioridade.SUPERPREFERENCIAL then 0
                        when a.prioridade = br.com.filasaude.entities.enums.Prioridade.PREFERENCIAL then 1
                        else 2
                     end,
                     a.dataHoraChegada asc
            """)
    List<Atendimento> findAllAguardando();

    @Query(value = """
            select a.* from atendimentos a
            join filas f on f.id = a.fila_id
            where f.ativa = true and a.status = 'AGUARDANDO'
            order by case a.prioridade
                        when 'SUPERPREFERENCIAL' then 0
                        when 'PREFERENCIAL' then 1
                        else 2
                     end, a.data_hora_chegada asc
            limit 1
            for update of a skip locked
            """, nativeQuery = true)
    Optional<Atendimento> chamarProximoGeral();

    @Query("""
            select a from Atendimento a
            join fetch a.paciente
            join fetch a.fila
            left join fetch a.atendente
            where a.dataHoraChamada is not null
              and (a.dataReferencia = :data or a.status in (br.com.filasaude.entities.enums.StatusAtendimento.CHAMADO, br.com.filasaude.entities.enums.StatusAtendimento.EM_ATENDIMENTO))
            order by a.dataHoraChamada desc
            """)
    List<Atendimento> findChamadosDoDia(LocalDate data);

    @Query("""
            select a from Atendimento a
            join fetch a.paciente
            join fetch a.fila
            left join fetch a.atendente
            where a.dataReferencia = :data
              and (:filaId is null or a.fila.id = :filaId)
            order by a.dataHoraChegada desc
            """)
    List<Atendimento> findDoDia(LocalDate data, Long filaId);

    @Query("""
            select a from Atendimento a
            join fetch a.paciente
            join fetch a.fila
            left join fetch a.atendente
            where a.status in :status
            order by a.dataHoraChamada desc
            """)
    List<Atendimento> findByStatusIn(Collection<StatusAtendimento> status);

    @Query("""
            select a from Atendimento a
            join fetch a.paciente
            join fetch a.fila
            left join fetch a.atendente
            where a.paciente.id = :pacienteId
            order by a.dataHoraChegada desc
            """)
    List<Atendimento> findByPacienteIdOrderByDataHoraChegadaDesc(Long pacienteId);

    boolean existsByPacienteIdAndStatusIn(Long pacienteId, Collection<StatusAtendimento> status);

    boolean existsByPacienteId(Long pacienteId);

    boolean existsByFilaId(Long filaId);

    boolean existsByAtendenteId(Long usuarioId);
}
