package com.correx.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CriterioRequest(
    @NotBlank(message = "Nome é obrigatório")
    String nome,

    String descricao,

    @NotNull(message = "Peso é obrigatório")
    @Min(value = 1, message = "Peso deve ser no mínimo 1")
    @Max(value = 100, message = "Peso deve ser no máximo 100")
    Integer peso,

    Boolean ativo
) {}
