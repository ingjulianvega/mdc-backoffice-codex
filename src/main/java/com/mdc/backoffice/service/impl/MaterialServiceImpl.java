package com.mdc.backoffice.service.impl;

import com.mdc.backoffice.exception.ResourceNotFoundException;
import com.mdc.backoffice.mapper.MaterialMapper;
import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.model.entity.MaterialEntity;
import com.mdc.backoffice.repository.*;
import com.mdc.backoffice.service.MaterialService;
import com.mdc.backoffice.specification.MaterialSpecification;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MaterialServiceImpl implements MaterialService {
    private final MaterialRepository repository;
    private final MaterialMapper mapper;

    @Override
    @Transactional
    public MaterialResponseDTO create(MaterialRequestDTO request) {
        var entity = mapper.toEntity(request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MaterialResponseDTO> findAll(String name, Pageable pageable) {
        return repository.findAll(MaterialSpecification.hasName(name), pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public MaterialResponseDTO findById(UUID id) {
        return mapper.toResponse(requireEntity(id));
    }

    @Override
    @Transactional
    public MaterialResponseDTO update(UUID id, MaterialRequestDTO request) {
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

    private MaterialEntity requireEntity(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material", id));
    }
}

