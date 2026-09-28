package com.mdc.backoffice.model.dto;
import java.util.UUID;
import java.time.Instant;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "Customer response")
public record CustomerResponseDTO(
    UUID id,
    String documentType,
    String documentNumber,
    String firstName,
    String lastName,
    String phoneNumber,
    String email,
    Instant createdAt,
    String createdBy,
    Instant updatedAt,
    String updatedBy) {}
