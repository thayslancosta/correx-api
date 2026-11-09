package com.correx.mapper;

import com.correx.dto.CriterioResponse;
import com.correx.entity.CriterioAvaliacao;

public class CriterioMapper {

    public static CriterioResponse toResponse(CriterioAvaliacao criterio) {
        return new CriterioResponse(criterio.getId(), criterio.getNome(), criterio.getDescricao(),
            criterio.getPeso(), criterio.getAtivo());
    }
}
