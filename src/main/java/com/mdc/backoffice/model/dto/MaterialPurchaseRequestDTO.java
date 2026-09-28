package com.mdc.backoffice.model.dto;

import java.time.Instant;
import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Compra de materiales; la fecha omitida usa el instante actual")
public record MaterialPurchaseRequestDTO(
        @NotNull UUID supplierId,
        Instant purchaseDate,
        @NotEmpty List<@NotNull @Valid MaterialPurchaseItemRequestDTO> items) {}
