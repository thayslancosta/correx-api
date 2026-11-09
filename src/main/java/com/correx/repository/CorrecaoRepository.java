package com.correx.repository;

import com.correx.entity.Correcao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CorrecaoRepository extends JpaRepository<Correcao, Long> {
    Optional<Correcao> findByRedacaoId(Long redacaoId);
}
