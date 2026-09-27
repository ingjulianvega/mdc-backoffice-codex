package com.mdc.backoffice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.util.UUID;

@Schema(description = "Datos para crear o actualizar City.")
public record CityRequestDTO(
        @Schema(description = "Nombre no vacio", example = "Medellin", minLength = 1, maxLength = 100,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(min = 1, max = 100) String name,
        @Schema(description = "UUID de un departamento existente", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull UUID departmentId) {
}
