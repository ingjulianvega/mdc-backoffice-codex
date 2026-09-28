package com.mdc.backoffice.model.dto;
import java.util.UUID;
import java.time.Instant;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "Supplier request")
public record SupplierRequestDTO(
    @NotBlank @Size(max = 150) String companyName,
    @Size(max = 100) String contactName,
    @Email @Size(max = 150) String email,
    @Size(max = 30) String phone,
    @NotBlank @Size(max = 30) String taxId) {}
