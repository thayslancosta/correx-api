package com.correx.mapper;

import com.correx.dto.AlunoOptionResponse;
import com.correx.dto.AlunoResponse;
import com.correx.entity.Aluno;

public class AlunoMapper {

    public static AlunoResponse toResponse(Aluno aluno, Double media, int totalRedacoes, String ultimaAtividade) {
        return new AlunoResponse(aluno.getId(), aluno.getNome(), aluno.getEmail(),
            aluno.getTurma().getNome(), media, totalRedacoes, ultimaAtividade);
    }

    public static AlunoOptionResponse toOptionResponse(Aluno aluno) {
        return new AlunoOptionResponse(aluno.getId(), aluno.getNome());
    }
}
