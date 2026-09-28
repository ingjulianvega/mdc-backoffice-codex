package com.mdc.backoffice.service;

import com.mdc.backoffice.model.dto.*;
import java.util.UUID;
import org.springframework.data.domain.*;

public interface ProductRecipeService {
    ProductRecipeResponseDTO create(ProductRecipeRequestDTO request);
    Page<ProductRecipeResponseDTO> findAll(UUID productId, UUID materialId, Pageable pageable);
    ProductRecipeResponseDTO findById(UUID id);
    ProductRecipeResponseDTO update(UUID id, ProductRecipeRequestDTO request);
    void delete(UUID id);
}

