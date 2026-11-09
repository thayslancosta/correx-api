package com.correx.repository;

import com.correx.entity.CriterioAvaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CriterioAvaliacaoRepository extends JpaRepository<CriterioAvaliacao, Long> {
    List<CriterioAvaliacao> findByProfessorId(Long professorId);
    List<CriterioAvaliacao> findByProfessorIdAndAtivoTrue(Long professorId);
    Optional<CriterioAvaliacao> findByIdAndProfessorId(Long id, Long professorId);
}
