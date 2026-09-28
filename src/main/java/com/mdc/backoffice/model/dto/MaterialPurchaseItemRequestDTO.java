package com.mdc.backoffice.model.dto;

import java.util.UUID;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Material y coste de una linea de compra")
public record MaterialPurchaseItemRequestDTO(
        @NotNull UUID materialId,
        @NotNull @Positive @Digits(integer = 8, fraction = 4) Double quantity,
        @NotNull @Positive @Digits(integer = 8, fraction = 4) Double unitCost) {}
