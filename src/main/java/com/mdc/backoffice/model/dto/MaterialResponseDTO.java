package com.mdc.backoffice.model.dto;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.Instant;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Material response")
public record MaterialResponseDTO(
        UUID id,
        String name,
        String unitOfMeasure,
        BigDecimal currentStock,
        Instant createdAt,
        String createdBy,
        Instant updatedAt,
        String updatedBy) {
}

