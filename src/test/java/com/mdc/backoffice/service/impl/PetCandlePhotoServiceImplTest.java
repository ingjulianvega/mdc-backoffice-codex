package com.mdc.backoffice.service.impl;

import com.mdc.backoffice.exception.*;
import com.mdc.backoffice.mapper.*;
import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.model.entity.*;
import com.mdc.backoffice.repository.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mapstruct.factory.Mappers;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class PetCandlePhotoServiceImplTest {
    @Mock PetCandlePhotoRepository repository;
    @Mock PetCandleRepository candles;
    PetCandlePhotoServiceImpl service;
    UUID id = UUID.randomUUID();
    @BeforeEach void setup() {
        service = new PetCandlePhotoServiceImpl(repository, candles, Mappers.getMapper(PetCandlePhotoMapper.class));
    }
    @Test void addsPhotoAndPreservesOptionalPrimaryFlag() {
        var candle = new PetCandleEntity(); candle.setId(id);
        when(candles.findById(id)).thenReturn(Optional.of(candle));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        for (Boolean primary : Arrays.asList(true, false, null)) {
            var result = service.create(new PetCandlePhotoRequestDTO(id, "https://example.com/p.jpg", primary));
            assertEquals(id, result.petCandleId());
            assertEquals("https://example.com/p.jpg", result.photoUrl());
            assertEquals(primary, result.isPrimary());
        }
    }
    @Test void listsPhotosAndDeletesExactPhoto() {
        var candle = new PetCandleEntity(); candle.setId(id);
        var photo = new PetCandlePhotoEntity(); photo.setId(UUID.randomUUID()); photo.setPetCandle(candle);
        when(candles.existsById(id)).thenReturn(true);
        when(repository.findAllByPetCandleId(id)).thenReturn(List.of(photo));
        when(repository.findById(photo.getId())).thenReturn(Optional.of(photo));
        assertEquals(photo.getId(), service.findByPetCandleId(id).getFirst().id());
        service.delete(photo.getId()); verify(repository).delete(photo);
    }
    @Test void existingCandleMayHaveNoPhotos() {
        when(candles.existsById(id)).thenReturn(true);
        assertTrue(service.findByPetCandleId(id).isEmpty());
    }
    @Test void rejectsMissingParentOrPhoto() {
        assertThrows(ResourceNotFoundException.class, () -> service.create(new PetCandlePhotoRequestDTO(id, "url", true)));
        assertThrows(ResourceNotFoundException.class, () -> service.findByPetCandleId(id));
        assertThrows(ResourceNotFoundException.class, () -> service.delete(id));
        verify(repository, never()).save(any()); verify(repository, never()).delete(any());
    }
}
