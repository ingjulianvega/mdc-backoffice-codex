package com.mdc.backoffice.service;

import com.mdc.backoffice.model.dto.*;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.*;

public interface OrderService {
    OrderResponseDTO create(OrderRequestDTO request);
    Page<OrderResponseDTO> findAll(UUID customerId, Instant startDate, Instant endDate, Pageable pageable);
    OrderResponseDTO findById(UUID id);
    void delete(UUID id);
}
