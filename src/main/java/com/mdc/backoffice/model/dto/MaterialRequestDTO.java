package com.mdc.backoffice.model.dto;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.Instant;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Material request")
public record MaterialRequestDTO(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 50) String unitOfMeasure,
        @NotNull @PositiveOrZero @Digits(integer = 8, fraction = 4) BigDecimal currentStock) {
}

