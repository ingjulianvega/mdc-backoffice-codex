package com.mdc.backoffice.service.impl;

import com.mdc.backoffice.exception.ResourceNotFoundException;
import com.mdc.backoffice.mapper.DepartmentMapper;
import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.model.entity.DepartmentEntity;
import com.mdc.backoffice.repository.*;
import com.mdc.backoffice.service.DepartmentService;
import com.mdc.backoffice.specification.DepartmentSpecification;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository repository;
    private final DepartmentMapper mapper;


    @Override
    @Transactional
    public DepartmentResponseDTO create(DepartmentRequestDTO request) {

        var entity = mapper.toEntity(request);

        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DepartmentResponseDTO> findAll(Pageable pageable) {
        return repository.findAll(DepartmentSpecification.all(), pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponseDTO findById(UUID id) {
        return mapper.toResponse(requireEntity(id));
    }

    @Override
    @Transactional
    public DepartmentResponseDTO update(UUID id, DepartmentRequestDTO request) {
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

    private DepartmentEntity requireEntity(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
    }
}
