package com.mdc.backoffice.model.dto;
import java.util.UUID;
import java.time.Instant;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "Supplier response")
public record SupplierResponseDTO(
    UUID id,
    String companyName,
    String contactName,
    String email,
    String phone,
    String taxId,
    Instant createdAt,
    String createdBy,
    Instant updatedAt,
    String updatedBy) {}
