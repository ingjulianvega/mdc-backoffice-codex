package com.mdc.backoffice.service;

import com.mdc.backoffice.model.dto.*;
import java.util.UUID;
import org.springframework.data.domain.*;

public interface MaterialService {
    MaterialResponseDTO create(MaterialRequestDTO request);
    Page<MaterialResponseDTO> findAll(String name, Pageable pageable);
    MaterialResponseDTO findById(UUID id);
    MaterialResponseDTO update(UUID id, MaterialRequestDTO request);
    void delete(UUID id);
}

