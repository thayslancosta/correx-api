package com.correx.repository;

import com.correx.entity.Redacao;
import com.correx.enums.StatusRedacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RedacaoRepository extends JpaRepository<Redacao, Long> {
    Page<Redacao> findByProfessorId(Long professorId, Pageable pageable);
    Page<Redacao> findByAlunoIdAndProfessorId(Long alunoId, Long professorId, Pageable pageable);
    Page<Redacao> findByProfessorIdAndStatus(Long professorId, StatusRedacao status, Pageable pageable);
    long countByProfessorId(Long professorId);
    long countByProfessorIdAndStatus(Long professorId, StatusRedacao status);
    List<Redacao> findTop5ByProfessorIdOrderByCreatedAtDesc(Long professorId);
    Optional<Redacao> findByIdAndProfessorId(Long id, Long professorId);
}
