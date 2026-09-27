package com.mdc.backoffice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.util.UUID;

@Schema(description = "Datos para crear o actualizar Department.")
public record DepartmentRequestDTO(
        @Schema(description = "Nombre no vacio", example = "Antioquia", minLength = 1, maxLength = 100,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(min = 1, max = 100) String name) {
}
