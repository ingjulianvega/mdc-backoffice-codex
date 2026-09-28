package com.mdc.backoffice.service.impl;

import com.mdc.backoffice.exception.ResourceNotFoundException;
import com.mdc.backoffice.mapper.PetCandlePhotoMapper;
import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.repository.*;
import com.mdc.backoffice.service.PetCandlePhotoService;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PetCandlePhotoServiceImpl implements PetCandlePhotoService {
    private final PetCandlePhotoRepository repository;
    private final PetCandleRepository candles;
    private final PetCandlePhotoMapper mapper;

    @Override
    public PetCandlePhotoResponseDTO create(PetCandlePhotoRequestDTO request) {
        var candle = candles.findById(request.petCandleId())
                .orElseThrow(() -> new ResourceNotFoundException("Pet candle", request.petCandleId()));
        var photo = mapper.toEntity(request);
        photo.setPetCandle(candle);
        return mapper.toResponse(repository.save(photo));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PetCandlePhotoResponseDTO> findByPetCandleId(UUID petCandleId) {
        if (!candles.existsById(petCandleId)) {
            throw new ResourceNotFoundException("Pet candle", petCandleId);
        }
        return repository.findAllByPetCandleId(petCandleId).stream().map(mapper::toResponse).toList();
    }

    @Override
    public void delete(UUID id) {
        var photo = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pet candle photo", id));
        repository.delete(photo);
    }
}
