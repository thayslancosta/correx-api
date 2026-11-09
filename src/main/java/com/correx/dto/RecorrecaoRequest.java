package com.correx.dto;

import jakarta.validation.constraints.NotBlank;

public record RecorrecaoRequest(
    @NotBlank(message = "Texto editado é obrigatório")
    String textoEditado
) {}
