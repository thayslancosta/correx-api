package com.correx.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notas_criterio")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotaCriterio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nomeCriterio;

    @Column(nullable = false)
    private Integer peso;

    @Column(nullable = false)
    private Double nota;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "correcao_id", nullable = false)
    private Correcao correcao;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Integer version;
}
