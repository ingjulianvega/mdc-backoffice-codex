package com.mdc.backoffice.service.impl;

import com.mdc.backoffice.exception.ResourceNotFoundException;
import com.mdc.backoffice.mapper.SupplierMapper;
import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.model.entity.SupplierEntity;
import com.mdc.backoffice.repository.*;
import com.mdc.backoffice.service.SupplierService;
import com.mdc.backoffice.specification.SupplierSpecification;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {
    private final SupplierRepository repository;
    private final SupplierMapper mapper;

    @Override
    @Transactional
    public SupplierResponseDTO create(SupplierRequestDTO request) {
        validateUnique(request, null);
        var entity = mapper.toEntity(request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupplierResponseDTO> findAll(String companyName, String taxId, Pageable pageable) {
        return repository.findAll(SupplierSpecification.filter(companyName, taxId), pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponseDTO findById(UUID id) {
        return mapper.toResponse(requireEntity(id));
    }

    @Override
    @Transactional
    public SupplierResponseDTO update(UUID id, SupplierRequestDTO request) {
        var entity = requireEntity(id);
        validateUnique(request, id);
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

    private void validateUnique(SupplierRequestDTO request, UUID id) {
        if (request.email() != null && (id == null
                ? repository.existsByEmailIgnoreCase(request.email())
                : repository.existsByEmailIgnoreCaseAndIdNot(request.email(), id))) {
            throw new com.mdc.backoffice.exception.DuplicateResourceException("Supplier email already exists.");
        }
        if (request.taxId() != null && (id == null
                ? repository.existsByTaxId(request.taxId())
                : repository.existsByTaxIdAndIdNot(request.taxId(), id))) {
            throw new com.mdc.backoffice.exception.DuplicateResourceException("Supplier taxId already exists.");
        }
    }

    private SupplierEntity requireEntity(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));
    }
}
