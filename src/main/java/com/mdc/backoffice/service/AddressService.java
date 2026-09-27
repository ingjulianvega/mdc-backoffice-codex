package com.mdc.backoffice.service;

import com.mdc.backoffice.model.dto.*;
import java.util.UUID;
import org.springframework.data.domain.*;

public interface AddressService {
    AddressResponseDTO create(AddressRequestDTO request);
    Page<AddressResponseDTO> findAll(UUID cityId, UUID customerId, Pageable pageable);
    AddressResponseDTO findById(UUID id);
    AddressResponseDTO update(UUID id, AddressRequestDTO request);
    void delete(UUID id);
}
