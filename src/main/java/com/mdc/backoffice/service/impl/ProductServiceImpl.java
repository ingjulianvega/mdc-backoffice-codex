package com.mdc.backoffice.service.impl;

import com.mdc.backoffice.exception.ResourceNotFoundException;
import com.mdc.backoffice.mapper.ProductMapper;
import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.model.entity.ProductEntity;
import com.mdc.backoffice.repository.*;
import com.mdc.backoffice.service.ProductService;
import com.mdc.backoffice.specification.ProductSpecification;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository repository;
    private final ProductMapper mapper;

    @Override
    @Transactional
    public ProductResponseDTO create(ProductRequestDTO request) {
        var entity = mapper.toEntity(request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> findAll(String name, Pageable pageable) {
        return repository.findAll(ProductSpecification.hasName(name), pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO findById(UUID id) {
        return mapper.toResponse(requireEntity(id));
    }

    @Override
    @Transactional
    public ProductResponseDTO update(UUID id, ProductRequestDTO request) {
        var entity = requireEntity(id);
        mapper.update(request, entity);
        var saved = repository.save(entity);
        repository.flush();
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        repository.delete(requireEntity(id));
    }

    private ProductEntity requireEntity(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }
}

