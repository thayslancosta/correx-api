package com.correx.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ApiErrorResponse(
    int status,
    String code,
    String message,
    LocalDateTime timestamp,
    List<String> errors
) {}
