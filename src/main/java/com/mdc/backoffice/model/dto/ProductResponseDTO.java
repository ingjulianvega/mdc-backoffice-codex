package com.mdc.backoffice.model.dto;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.Instant;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Product response")
public record ProductResponseDTO(
        UUID id,
        String name,
        String description,
        Long price,
        Integer stockQuantity,
        Integer minStockAlert,
        Instant createdAt,
        String createdBy,
        Instant updatedAt,
        String updatedBy) {
}

