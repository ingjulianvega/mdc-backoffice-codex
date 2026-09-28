package com.mdc.backoffice.service.impl;

import com.mdc.backoffice.exception.ResourceNotFoundException;
import com.mdc.backoffice.mapper.ProductRecipeMapper;
import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.model.entity.ProductRecipeEntity;
import com.mdc.backoffice.repository.*;
import com.mdc.backoffice.service.ProductRecipeService;
import com.mdc.backoffice.specification.ProductRecipeSpecification;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductRecipeServiceImpl implements ProductRecipeService {
    private final ProductRecipeRepository repository;
    private final ProductRecipeMapper mapper;
    private final ProductRepository productRepository;
    private final MaterialRepository materialRepository;

    @Override
    @Transactional
    public ProductRecipeResponseDTO create(ProductRecipeRequestDTO request) {
        var product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.productId()));
        var material = materialRepository.findById(request.materialId())
                .orElseThrow(() -> new ResourceNotFoundException("Material", request.materialId()));
        var entity = mapper.toEntity(request);
        entity.setProduct(product);
        entity.setMaterial(material);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductRecipeResponseDTO> findAll(UUID productId, UUID materialId, Pageable pageable) {
        return repository.findAll(ProductRecipeSpecification.hasProductId(productId).and(ProductRecipeSpecification.hasMaterialId(materialId)), pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductRecipeResponseDTO findById(UUID id) {
        return mapper.toResponse(requireEntity(id));
    }

    @Override
    @Transactional
    public ProductRecipeResponseDTO update(UUID id, ProductRecipeRequestDTO request) {
        var entity = requireEntity(id);
        var product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.productId()));
        var material = materialRepository.findById(request.materialId())
                .orElseThrow(() -> new ResourceNotFoundException("Material", request.materialId()));
        mapper.update(request, entity);
        entity.setProduct(product);
        entity.setMaterial(material);
        var saved = repository.save(entity);
        repository.flush();
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        repository.delete(requireEntity(id));
    }

    private ProductRecipeEntity requireEntity(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductRecipe", id));
    }
}

