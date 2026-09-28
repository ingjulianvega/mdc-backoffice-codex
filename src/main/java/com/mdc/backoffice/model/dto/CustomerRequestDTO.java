package com.mdc.backoffice.model.dto;
import java.util.UUID;
import java.time.Instant;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "Customer request")
public record CustomerRequestDTO(
    @NotBlank @Size(max = 50) String documentType,
    @NotBlank @Size(max = 50) String documentNumber,
    @NotBlank @Size(max = 100) String firstName,
    @NotBlank @Size(max = 100) String lastName,
    @Size(max = 30) String phoneNumber,
    @NotBlank @Email @Size(max = 255) String email) {}
