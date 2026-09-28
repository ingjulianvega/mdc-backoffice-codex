package com.mdc.backoffice.service;
import com.mdc.backoffice.model.dto.*;
import java.util.*;
public interface PetCandlePhotoService {
    PetCandlePhotoResponseDTO create(PetCandlePhotoRequestDTO request);
    List<PetCandlePhotoResponseDTO> findByPetCandleId(UUID petCandleId);
    void delete(UUID id);
}
