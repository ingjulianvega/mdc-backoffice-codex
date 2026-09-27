package com.mdc.backoffice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.net.URI;
import java.util.Map;

/** OpenAPI representation of the ProblemDetail responses emitted by the exception handler. */
@Schema(name = "ApiProblem", description = "Error ProblemDetail. errors contiene mensajes por campo en errores de validacion.")
public record ApiProblemDTO(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "urn:problem:http:400") URI type,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Bad Request") String title,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "400") int status,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String detail,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "/api/v1/cities") URI instance,
        @Schema(description = "Mapa campo -> mensaje; presente solo para errores de validacion.") Map<String, String> errors) {
}
