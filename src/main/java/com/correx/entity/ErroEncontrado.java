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
@Table(name = "erros_encontrados")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErroEncontrado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String categoria;

    @Lob
    @Column(nullable = false)
    private String textoOriginal;

    @Lob
    @Column(nullable = false)
    private String textoCorrigido;

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
