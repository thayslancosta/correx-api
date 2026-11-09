package com.correx.dto;

public record AlunoResponse(
    Long id,
    String nome,
    String email,
    String turma,
    Double media,
    int totalRedacoes,
    String ultimaAtividade
) {}
