package com.correx.dto;

import java.time.LocalDateTime;

public record RedacaoResumoResponse(
    Long id,
    String nomeAluno,
    String turma,
    Double nota,
    String status,
    LocalDateTime createdAt
) {}
