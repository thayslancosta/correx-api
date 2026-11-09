package com.correx.dto;

import java.time.LocalDateTime;
import java.util.List;

public record RedacaoResponse(
    Long id,
    String textoOriginal,
    String textoEditado,
    String idioma,
    String status,
    String origem,
    String nomeArquivoOriginal,
    String tipoArquivo,
    Long tamanhoArquivoBytes,
    Long alunoId,
    String nomeAluno,
    String turma,
    CorrecaoResponse correcao,
    LocalDateTime createdAt
) {}
