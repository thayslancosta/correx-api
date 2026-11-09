package com.correx.repository;

import com.correx.entity.Turma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TurmaRepository extends JpaRepository<Turma, Long> {
    List<Turma> findByProfessorId(Long professorId);
    List<Turma> findByProfessorIdAndAtivaTrue(Long professorId);
    Optional<Turma> findByIdAndProfessorId(Long id, Long professorId);
}
