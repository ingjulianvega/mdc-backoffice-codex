package com.mdc.backoffice.service;

import com.mdc.backoffice.model.dto.*;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.*;

public interface MaterialPurchaseService {
    MaterialPurchaseResponseDTO create(MaterialPurchaseRequestDTO request);
    Page<MaterialPurchaseResponseDTO> findAll(UUID supplierId, Instant startDate, Instant endDate, Pageable pageable);
    MaterialPurchaseResponseDTO findById(UUID id);
    void delete(UUID id);
}
