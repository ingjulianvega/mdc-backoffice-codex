package com.mdc.backoffice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.util.UUID;

@Schema(description = "Datos para crear o reemplazar una direccion.")
public record AddressRequestDTO(
        @Schema(example = "Calle 10", maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 255) String streetAddress,
        @Schema(example = "Apartamento 301", maxLength = 255)
        @Size(max = 255) String additionalInfo,
        @Schema(description = "UUID de una ciudad existente", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull UUID cityId,
        @Schema(description = "UUID del cliente", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull UUID customerId) {
}