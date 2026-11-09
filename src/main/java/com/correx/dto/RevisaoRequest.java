package com.correx.dto;

public record RevisaoRequest(
    String textoEditado,

    Double notaFinalManual,

    String feedbackExtra
) {}
