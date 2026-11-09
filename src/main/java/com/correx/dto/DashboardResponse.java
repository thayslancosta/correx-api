package com.correx.dto;

import java.util.List;

public record DashboardResponse(
    long totalCorrecoes,
    long alunosAtivos,
    Double mediaGeral,
    long pendentes,
    List<DashboardCorrecaoResumoResponse> correcoesRecentes
) {}
