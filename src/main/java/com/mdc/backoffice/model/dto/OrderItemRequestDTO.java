package com.mdc.backoffice.model.dto;
import java.util.UUID;
import jakarta.validation.constraints.*;
public record OrderItemRequestDTO(
        @NotNull UUID productId, @NotNull @Positive Integer quantity,
        @NotNull @PositiveOrZero Long unitPrice, UUID addressId,
        @Size(max = 100) String trackingNumber, @Size(max = 100) String carrierName) {}