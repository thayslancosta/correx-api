package com.correx.dto;

public record ErroResponse(
    String categoria,
    String textoOriginal,
    String textoCorrigido
) {}
