package com.correx.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.correx.enums.StatusRedacao;
import com.correx.enums.OrigemRedacao;

import java.time.LocalDateTime;

@Entity
@Table(name = "redacoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Redacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(nullable = false)
    private String textoOriginal;

    @Lob
    private String textoEditado;

    @Column(nullable = false, length = 10)
    private String idioma;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusRedacao status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrigemRedacao origem;

    @Column(length = 255)
    private String nomeArquivoOriginal;

    @Column(length = 50)
    private String tipoArquivo;

    @Column
    private Long tamanhoArquivoBytes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id", nullable = false)
    private Professor professor;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Integer version;

    @OneToOne(mappedBy = "redacao", fetch = FetchType.LAZY)
    @JsonIgnore
    private Correcao correcao;
}
