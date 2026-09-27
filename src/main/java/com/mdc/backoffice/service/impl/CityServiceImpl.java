package com.mdc.backoffice.service.impl;

import com.mdc.backoffice.exception.ResourceNotFoundException;
import com.mdc.backoffice.mapper.CityMapper;
import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.model.entity.CityEntity;
import com.mdc.backoffice.repository.*;
import com.mdc.backoffice.service.CityService;
import com.mdc.backoffice.specification.CitySpecification;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CityServiceImpl implements CityService {
    private final CityRepository repository;
    private final CityMapper mapper;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public CityResponseDTO create(CityRequestDTO request) {
        var department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", request.departmentId()));
        var entity = mapper.toEntity(request);
        entity.setDepartment(department);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CityResponseDTO> findAll(UUID departmentId, Pageable pageable) {
        return repository.findAll(CitySpecification.hasDepartmentId(departmentId), pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CityResponseDTO findById(UUID id) {
        return mapper.toResponse(requireEntity(id));
    }

    @Override
    @Transactional
    public CityResponseDTO update(UUID id, CityRequestDTO request) {
        var entity = requireEntity(id);
        var department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", request.departmentId()));
        mapper.update(request, entity);
        entity.setDepartment(department);
        var saved = repository.save(entity);
        repository.flush();
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        repository.delete(requireEntity(id));
    }

    private CityEntity requireEntity(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City", id));
    }
}
