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
            left join fetch a.chamadoPor
            where a.id = :id
            """)
    Optional<Atendimento> findByIdDetalhado(Long id);

    @Query("""
            select coalesce(max(a.numero), 0) from Atendimento a
            where a.fila.id = :filaId and a.dataReferencia = :data
            """)
    int findUltimoNumero(Long filaId, LocalDate data);

    /** Id da fila do atendimento, sem carregar a entidade (permite travar a fila antes de ler o atendimento). */
    @Query("select a.fila.id from Atendimento a where a.id = :id")
    Optional<Long> findFilaIdById(Long id);

    /** Pacientes aguardando na fila, por ordem de chegada. A prioridade é aplicada pelo AtendimentoService. */
    @Query("""
            select a from Atendimento a
            join fetch a.paciente
            join fetch a.fila
            where a.fila.id = :filaId and a.status = br.com.filasaude.entities.enums.StatusAtendimento.AGUARDANDO
            order by a.dataHoraChegada asc
            """)
    List<Atendimento> findAguardandoNaFila(Long filaId);

    /** Todos os pacientes aguardando em filas ativas, por ordem de chegada. A prioridade é aplicada pelo AtendimentoService. */
    @Query("""
            select a from Atendimento a
            join fetch a.paciente
            join fetch a.fila
            where a.fila.ativa = true and a.status = br.com.filasaude.entities.enums.StatusAtendimento.AGUARDANDO
            order by a.dataHoraChegada asc
            """)
    List<Atendimento> findAllAguardando();

    /** Histórico de chamadas do dia (mais recentes primeiro) para cálculo da intercalação 2x1. */
    @Query("""
            select a from Atendimento a
            where a.dataReferencia = :data
              and a.dataHoraChamada is not null
              and (:filaId is null or a.fila.id = :filaId)
            order by a.dataHoraChamada desc, a.id desc
            """)
    List<Atendimento> findUltimosChamadosDoDia(LocalDate data, Long filaId);

    @Query("""
            select a from Atendimento a
            join fetch a.paciente
            join fetch a.fila
            left join fetch a.atendente
            left join fetch a.chamadoPor
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
            left join fetch a.chamadoPor
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
            left join fetch a.chamadoPor
            where a.status in :status
            order by a.dataHoraChamada desc
            """)
    List<Atendimento> findByStatusIn(Collection<StatusAtendimento> status);

    @Query("""
            select a from Atendimento a
            join fetch a.paciente
            join fetch a.fila
            left join fetch a.atendente
            left join fetch a.chamadoPor
            where a.paciente.id = :pacienteId
            order by a.dataHoraChegada desc
            """)
    List<Atendimento> findByPacienteIdOrderByDataHoraChegadaDesc(Long pacienteId);

    boolean existsByPacienteIdAndStatusIn(Long pacienteId, Collection<StatusAtendimento> status);

    boolean existsByPacienteId(Long pacienteId);

    boolean existsByFilaId(Long filaId);

    boolean existsByAtendenteId(Long usuarioId);
}
