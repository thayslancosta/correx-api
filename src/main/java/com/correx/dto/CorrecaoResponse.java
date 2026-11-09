package com.correx.dto;

import java.util.List;

public record CorrecaoResponse(
    Long id,
    Double notaFinal,
    Double notaFinalManual,
    String comentarioGeral,
    String feedbackExtra,
    List<NotaCriterioResponse> notas,
    List<ErroResponse> erros
) {}
