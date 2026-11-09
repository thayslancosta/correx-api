package com.correx.repository;

import com.correx.entity.Aluno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlunoRepository extends JpaRepository<Aluno, Long> {
    List<Aluno> findByTurmaId(Long turmaId);
    Page<Aluno> findByTurmaId(Long turmaId, Pageable pageable);
    List<Aluno> findByTurmaProfessorId(Long professorId);
    Page<Aluno> findByTurmaProfessorId(Long professorId, Pageable pageable);
    List<Aluno> findByTurmaProfessorIdAndAtivoTrue(Long professorId);
    List<Aluno> findByNomeContainingIgnoreCaseAndTurmaProfessorId(String nome, Long professorId);
    Page<Aluno> findByNomeContainingIgnoreCaseAndTurmaProfessorId(String nome, Long professorId, Pageable pageable);
    Optional<Aluno> findByIdAndTurmaProfessorId(Long id, Long professorId);

    // Métodos para evitar N+1 Query
    @Query("SELECT COUNT(r) FROM Redacao r WHERE r.aluno.id = :alunoId")
    int countRedacoesByAlunoId(@Param("alunoId") Long alunoId);

    @Query("SELECT AVG(c.notaFinal) FROM Correcao c WHERE c.redacao.aluno.id = :alunoId AND c.notaFinal IS NOT NULL")
    Double calcularMediaByAlunoId(@Param("alunoId") Long alunoId);

    @Query("SELECT MAX(r.createdAt) FROM Redacao r WHERE r.aluno.id = :alunoId")
    LocalDateTime ultimaAtividadeByAlunoId(@Param("alunoId") Long alunoId);
}
