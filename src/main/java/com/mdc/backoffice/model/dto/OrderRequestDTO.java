package com.mdc.backoffice.model.dto;
import java.time.Instant;
import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
public record OrderRequestDTO(@NotNull UUID customerId, @NotNull Instant orderDate,
        @NotEmpty List<@NotNull @Valid OrderItemRequestDTO> items) {}