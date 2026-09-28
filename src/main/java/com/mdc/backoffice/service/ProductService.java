package com.mdc.backoffice.service;

import com.mdc.backoffice.model.dto.*;
import java.util.UUID;
import org.springframework.data.domain.*;

public interface ProductService {
    ProductResponseDTO create(ProductRequestDTO request);
    Page<ProductResponseDTO> findAll(String name, Pageable pageable);
    ProductResponseDTO findById(UUID id);
    ProductResponseDTO update(UUID id, ProductRequestDTO request);
    void delete(UUID id);
}

