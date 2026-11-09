package com.correx.dto;

import java.time.LocalDateTime;

public record DashboardCorrecaoResumoResponse(
    Long id,
    String aluno,
    String turma,
    Double nota,
    LocalDateTime data,
    String status
) {}
