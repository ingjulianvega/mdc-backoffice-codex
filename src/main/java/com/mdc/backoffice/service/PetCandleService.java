package com.mdc.backoffice.service;
import com.mdc.backoffice.model.dto.*;
import java.util.UUID;
public interface PetCandleService {
    PetCandleResponseDTO create(PetCandleCreateDTO request);
    PetCandleResponseDTO findById(UUID id);
    PetCandleResponseDTO findByOrderItemId(UUID orderItemId);
    PetCandleResponseDTO update(UUID id, PetCandleUpdateDTO request);
    PetCandleResponseDTO updateStatuses(UUID id, PetCandleStatusUpdateDTO request);
}
