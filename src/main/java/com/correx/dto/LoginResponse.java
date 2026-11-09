package com.correx.dto;

public record LoginResponse(
    String token,
    String nome,
    String email
) {}
