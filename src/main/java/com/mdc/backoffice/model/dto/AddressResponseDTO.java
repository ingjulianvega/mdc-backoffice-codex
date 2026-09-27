package com.mdc.backoffice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Direccion persistida con ciudad y auditoria.")
public record AddressResponseDTO(
        UUID id, String streetAddress, String additionalInfo,
        CityResponseDTO city, UUID customerId, Instant createdAt, Instant updatedAt) {
}