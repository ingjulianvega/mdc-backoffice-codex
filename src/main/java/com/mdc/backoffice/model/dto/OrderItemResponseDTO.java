package com.mdc.backoffice.model.dto;
import java.util.UUID;
public record OrderItemResponseDTO(UUID id, UUID productId, String productName,
        Integer quantity, Long unitPrice, Long subtotal, UUID addressId,
        String trackingNumber, String carrierName) {}