package com.mdc.backoffice.model.dto;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.Instant;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Product request")
public record ProductRequestDTO(
        @NotBlank @Size(max = 150) String name,
        String description,
        @NotNull @Positive Long price,
        @NotNull @Min(0) Integer stockQuantity,
        @Min(0) Integer minStockAlert) {
}

