package com.mdc.backoffice.service;

import com.mdc.backoffice.model.dto.*;
import java.util.UUID;
import org.springframework.data.domain.*;

public interface SupplierService {
    SupplierResponseDTO create(SupplierRequestDTO request);
    Page<SupplierResponseDTO> findAll(String companyName, String taxId, Pageable pageable);
    SupplierResponseDTO findById(UUID id);
    SupplierResponseDTO update(UUID id, SupplierRequestDTO request);
    void delete(UUID id);
}
