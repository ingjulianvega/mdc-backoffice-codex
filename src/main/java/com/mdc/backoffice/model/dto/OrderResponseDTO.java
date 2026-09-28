package com.mdc.backoffice.model.dto;
import java.time.Instant;
import java.util.*;
public record OrderResponseDTO(UUID id, CustomerResponseDTO customer, Instant orderDate,
        Long totalAmount, List<OrderItemResponseDTO> items, String status, Instant createdAt) {}
