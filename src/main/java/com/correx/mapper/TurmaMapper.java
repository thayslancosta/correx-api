package com.correx.mapper;

import com.correx.dto.TurmaOptionResponse;
import com.correx.dto.TurmaResponse;
import com.correx.entity.Turma;

public class TurmaMapper {

    public static TurmaResponse toResponse(Turma turma, int totalAlunos, Double mediaGeral) {
        return new TurmaResponse(turma.getId(), turma.getNome(), turma.getAtiva(), totalAlunos, mediaGeral);
    }

    public static TurmaOptionResponse toOptionResponse(Turma turma) {
        return new TurmaOptionResponse(turma.getId(), turma.getNome());
    }
}
