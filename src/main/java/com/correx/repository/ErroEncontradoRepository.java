package com.correx.repository;

import com.correx.entity.ErroEncontrado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErroEncontradoRepository extends JpaRepository<ErroEncontrado, Long> {
    List<ErroEncontrado> findByCorrecaoId(Long correcaoId);
    void deleteByCorrecaoId(Long correcaoId);
}
