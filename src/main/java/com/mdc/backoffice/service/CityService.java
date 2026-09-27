package com.mdc.backoffice.service;

import com.mdc.backoffice.model.dto.*;
import java.util.UUID;
import org.springframework.data.domain.*;

public interface CityService {
    CityResponseDTO create(CityRequestDTO request);
    Page<CityResponseDTO> findAll(UUID departmentId, Pageable pageable);
    CityResponseDTO findById(UUID id);
    CityResponseDTO update(UUID id, CityRequestDTO request);
    void delete(UUID id);
}
