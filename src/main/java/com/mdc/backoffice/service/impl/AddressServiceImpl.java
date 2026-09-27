package com.mdc.backoffice.service.impl;

import com.mdc.backoffice.exception.ResourceNotFoundException;
import com.mdc.backoffice.mapper.AddressMapper;
import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.model.entity.AddressEntity;
import com.mdc.backoffice.repository.*;
import com.mdc.backoffice.service.AddressService;
import com.mdc.backoffice.specification.AddressSpecification;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
    private final AddressRepository repository;
    private final AddressMapper mapper;
    private final CityRepository cityRepository;

    @Override
    @Transactional
    public AddressResponseDTO create(AddressRequestDTO request) {
        var city = cityRepository.findById(request.cityId())
                .orElseThrow(() -> new ResourceNotFoundException("City", request.cityId()));
        var entity = mapper.toEntity(request);
        entity.setCity(city);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AddressResponseDTO> findAll(UUID cityId, UUID customerId, Pageable pageable) {
        return repository.findAll(AddressSpecification.hasCityId(cityId).and(AddressSpecification.hasCustomerId(customerId)), pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponseDTO findById(UUID id) {
        return mapper.toResponse(requireEntity(id));
    }

    @Override
    @Transactional
    public AddressResponseDTO update(UUID id, AddressRequestDTO request) {
        var entity = requireEntity(id);
        var city = cityRepository.findById(request.cityId())
                .orElseThrow(() -> new ResourceNotFoundException("City", request.cityId()));
        mapper.update(request, entity);
        entity.setCity(city);
        var saved = repository.save(entity);
        repository.flush();
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        repository.delete(requireEntity(id));
    }

    private AddressEntity requireEntity(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address", id));
    }
}
