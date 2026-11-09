package com.correx.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.correx.enums.ProfessorRole;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "professores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Professor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProfessorRole role;

    @Column(nullable = false)
    private Boolean ativo;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Integer version;

    @OneToMany(mappedBy = "professor", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Turma> turmas;

    @OneToMany(mappedBy = "professor", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<CriterioAvaliacao> criterios;

    @OneToMany(mappedBy = "professor", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Redacao> redacoes;
}
