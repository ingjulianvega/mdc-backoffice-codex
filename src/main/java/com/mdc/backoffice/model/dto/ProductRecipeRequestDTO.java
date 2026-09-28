package com.mdc.backoffice.model.dto;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.Instant;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ProductRecipe request")
public record ProductRecipeRequestDTO(
        @NotNull UUID productId,
        @NotNull UUID materialId,
        @NotNull @Positive @Digits(integer = 8, fraction = 4) BigDecimal requiredQuantity) {
}

