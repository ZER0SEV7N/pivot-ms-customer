package com.pe.pivot.customer.infrastructure.adapter.in.web.dto;

import java.time.Instant;
import lombok.Builder;

/**
 * Standardized immutable Error Response DTO using Java 17 Record.
 */
@Builder
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
}