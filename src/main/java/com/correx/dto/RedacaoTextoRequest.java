package com.correx.dto;

import jakarta.validation.constraints.NotBlank;

public record RedacaoTextoRequest(
    @NotBlank(message = "Texto é obrigatório")
    String texto,

    @NotBlank(message = "Idioma é obrigatório")
    String idioma,

    Long turmaId,

    Long alunoId,

    String nomeAluno
) {}
