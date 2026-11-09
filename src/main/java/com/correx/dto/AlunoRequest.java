package com.correx.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AlunoRequest(
    @NotBlank(message = "Nome é obrigatório")
    String nome,

    @Email(message = "Email deve ser válido")
    String email,

    @NotNull(message = "Turma é obrigatória")
    Long turmaId
) {}
