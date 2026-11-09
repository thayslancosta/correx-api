package com.correx.dto;

public record TurmaResponse(
    Long id,
    String nome,
    boolean ativa,
    int totalAlunos,
    Double mediaGeral
) {}
