package com.correx.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "correcoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Correcao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Double notaFinal;

    @Column
    private Double notaFinalManual;

    @Lob
    private String comentarioGeral;

    @Lob
    private String feedbackExtra;

    @Lob
    private String jsonRespostaIa;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "redacao_id", nullable = false, unique = true)
    private Redacao redacao;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Integer version;

    @OneToMany(mappedBy = "correcao", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<NotaCriterio> notas;

    @OneToMany(mappedBy = "correcao", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ErroEncontrado> erros;
}
