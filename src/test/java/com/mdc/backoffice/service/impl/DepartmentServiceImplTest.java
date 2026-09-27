package com.mdc.backoffice.service.impl;

import com.mdc.backoffice.exception.ResourceNotFoundException;
import com.mdc.backoffice.mapper.*;
import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.model.entity.*;
import com.mdc.backoffice.repository.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceImplTest {
    @Mock DepartmentRepository repository;
    @Mock DepartmentMapper mapper;

    @InjectMocks DepartmentServiceImpl service;
    final UUID id = UUID.randomUUID();
    final UUID departmentId = UUID.randomUUID();
    final DepartmentRequestDTO request = new DepartmentRequestDTO("Antioquia");
    final DepartmentResponseDTO response = new DepartmentResponseDTO(id, "Antioquia", null, null, null, null);
    DepartmentEntity entity;

    @BeforeEach
    void setUp() {
        entity = new DepartmentEntity();
        entity.setId(id);
        entity.setName("Antioquia");

    }

    @Test
    void createSavesAndMapsResult() {

        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);
        assertEquals(response, service.create(request));
        verify(repository).save(entity);
        verify(mapper).toEntity(request);
        verify(mapper).toResponse(entity);
    }

    @Test
    void findExistingIdMapsResult() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);
        assertEquals(response, service.findById(id));
    }

    @Test
    void missingIdThrowsNotFound() {
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findById(id));
        verifyNoInteractions(mapper);
    }

    @Test
    void updateMapsAndSavesExistingEntity() {

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);
        assertEquals(response, service.update(id, request));
        verify(mapper).update(request, entity);
        verify(repository).save(entity);
    }

    @Test
    void updateMissingIdDoesNotSave() {
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.update(id, request));
        verify(repository, never()).save(any());
    }

    @Test
    void deleteExistingEntity() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        service.delete(id);
        verify(repository).delete(entity);
    }

    @Test
    void deleteMissingIdDoesNotDelete() {
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.delete(id));
        verify(repository, never()).delete(any(DepartmentEntity.class));
    }

    @Test
    void listPassesPageableAndMapsPage() {
        var pageable = PageRequest.of(1, 2, Sort.by("name"));
        when(repository.findAll(ArgumentMatchers.<Specification<DepartmentEntity>>any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(entity), pageable, 3));
        when(mapper.toResponse(entity)).thenReturn(response);
        var result = service.findAll(pageable);
        assertEquals(List.of(response), result.getContent());
        assertEquals(3, result.getTotalElements());
        assertEquals(1, result.getNumber());
        verify(repository).findAll(ArgumentMatchers.<Specification<DepartmentEntity>>any(), eq(pageable));
    }

}
