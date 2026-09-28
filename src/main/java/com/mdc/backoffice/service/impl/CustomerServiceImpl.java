package com.mdc.backoffice.service.impl;

import com.mdc.backoffice.exception.ResourceNotFoundException;
import com.mdc.backoffice.mapper.CustomerMapper;
import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.model.entity.CustomerEntity;
import com.mdc.backoffice.repository.*;
import com.mdc.backoffice.service.CustomerService;
import com.mdc.backoffice.specification.CustomerSpecification;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository repository;
    private final CustomerMapper mapper;

    @Override
    @Transactional
    public CustomerResponseDTO create(CustomerRequestDTO request) {
        validateUnique(request, null);
        var entity = mapper.toEntity(request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponseDTO> findAll(String name, String email, String documentNumber, Pageable pageable) {
        return repository.findAll(CustomerSpecification.filter(name, email, documentNumber), pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponseDTO findById(UUID id) {
        return mapper.toResponse(requireEntity(id));
    }

    @Override
    @Transactional
    public CustomerResponseDTO update(UUID id, CustomerRequestDTO request) {
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

    private void validateUnique(CustomerRequestDTO request, UUID id) {
        if (request.documentNumber() != null && (id == null
                ? repository.existsByDocumentNumber(request.documentNumber())
                : repository.existsByDocumentNumberAndIdNot(request.documentNumber(), id))) {
            throw new com.mdc.backoffice.exception.DuplicateResourceException("Customer documentNumber already exists.");
        }
        if (request.email() != null && (id == null
                ? repository.existsByEmailIgnoreCase(request.email())
                : repository.existsByEmailIgnoreCaseAndIdNot(request.email(), id))) {
            throw new com.mdc.backoffice.exception.DuplicateResourceException("Customer email already exists.");
        }
    }

    private CustomerEntity requireEntity(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }
}
