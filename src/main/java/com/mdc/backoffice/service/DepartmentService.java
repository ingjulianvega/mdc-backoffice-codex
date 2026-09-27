package com.mdc.backoffice.service;

import com.mdc.backoffice.model.dto.*;
import java.util.UUID;
import org.springframework.data.domain.*;

public interface DepartmentService {
    DepartmentResponseDTO create(DepartmentRequestDTO request);
    Page<DepartmentResponseDTO> findAll(Pageable pageable);
    DepartmentResponseDTO findById(UUID id);
    DepartmentResponseDTO update(UUID id, DepartmentRequestDTO request);
    void delete(UUID id);
}
