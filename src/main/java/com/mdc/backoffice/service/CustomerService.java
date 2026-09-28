package com.mdc.backoffice.service;

import com.mdc.backoffice.model.dto.*;
import java.util.UUID;
import org.springframework.data.domain.*;

public interface CustomerService {
    CustomerResponseDTO create(CustomerRequestDTO request);
    Page<CustomerResponseDTO> findAll(String name, String email, String documentNumber, Pageable pageable);
    CustomerResponseDTO findById(UUID id);
    CustomerResponseDTO update(UUID id, CustomerRequestDTO request);
    void delete(UUID id);
}
