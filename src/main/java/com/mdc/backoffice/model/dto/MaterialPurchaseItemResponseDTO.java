package com.mdc.backoffice.model.dto;

import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detalle de material comprado")
public record MaterialPurchaseItemResponseDTO(UUID id, UUID materialId, String materialName,
        Double quantity, Double unitCost, Double subtotal) {}
