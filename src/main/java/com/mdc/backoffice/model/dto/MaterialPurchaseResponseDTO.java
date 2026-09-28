package com.mdc.backoffice.model.dto;

import java.time.Instant;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Compra con proveedor, importes calculados y detalle")
public record MaterialPurchaseResponseDTO(UUID id, SupplierResponseDTO supplier, Instant purchaseDate,
        Double totalCost, List<MaterialPurchaseItemResponseDTO> items, Instant createdAt) {}
