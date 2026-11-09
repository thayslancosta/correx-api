package com.correx.dto;

import jakarta.validation.constraints.NotBlank;

public record TurmaRequest(
    @NotBlank(message = "Nome é obrigatório")
    String nome,

    Boolean ativa
) {}
