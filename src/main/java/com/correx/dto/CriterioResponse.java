package com.correx.dto;

public record CriterioResponse(
    Long id,
    String nome,
    String descricao,
    int peso,
    boolean ativo
) {}
