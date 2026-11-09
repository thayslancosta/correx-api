package com.correx.mapper;

import com.correx.dto.DashboardCorrecaoResumoResponse;
import com.correx.entity.Redacao;

public class DashboardMapper {

    public static DashboardCorrecaoResumoResponse toResumoResponse(Redacao redacao) {
        Double nota = redacao.getCorrecao() != null ? redacao.getCorrecao().getNotaFinal() : null;
        return new DashboardCorrecaoResumoResponse(
            redacao.getId(),
            redacao.getAluno().getNome(),
            redacao.getAluno().getTurma().getNome(),
            nota,
            redacao.getCreatedAt(),
            redacao.getStatus().name()
        );
    }
}
