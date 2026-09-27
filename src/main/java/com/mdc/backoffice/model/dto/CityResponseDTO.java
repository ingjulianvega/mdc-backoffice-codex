package com.mdc.backoffice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "City persistido, con identificador y auditoria.")
public record CityResponseDTO(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Medellin", maxLength = 100) String name,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID departmentId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, type = "string", format = "date-time") Instant createdAt,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String createdBy,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, type = "string", format = "date-time") Instant updatedAt,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String updatedBy) {
}
