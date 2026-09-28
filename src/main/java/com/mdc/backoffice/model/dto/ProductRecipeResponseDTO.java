package com.mdc.backoffice.model.dto;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.Instant;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ProductRecipe response")
public record ProductRecipeResponseDTO(
        UUID id,
        UUID productId,
        UUID materialId,
        BigDecimal requiredQuantity,
        Instant createdAt,
        String createdBy,
        Instant updatedAt,
        String updatedBy) {
}

