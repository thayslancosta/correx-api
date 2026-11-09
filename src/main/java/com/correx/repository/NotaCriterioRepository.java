package com.correx.repository;

import com.correx.entity.NotaCriterio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotaCriterioRepository extends JpaRepository<NotaCriterio, Long> {
    List<NotaCriterio> findByCorrecaoId(Long correcaoId);
    void deleteByCorrecaoId(Long correcaoId);
}
