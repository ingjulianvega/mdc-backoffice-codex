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
class CityServiceImplTest {
    @Mock CityRepository repository;
    @Mock CityMapper mapper;
    @Mock DepartmentRepository departmentRepository;
    @InjectMocks CityServiceImpl service;
    final UUID id = UUID.randomUUID();
    final UUID departmentId = UUID.randomUUID();
    final CityRequestDTO request = new CityRequestDTO("Medellin", departmentId);
    final CityResponseDTO response = new CityResponseDTO(id, "Medellin", departmentId, null, null, null, null);
    CityEntity entity;

    @BeforeEach
    void setUp() {
        entity = new CityEntity();
        entity.setId(id);
        entity.setName("Medellin");
        var department = new DepartmentEntity();
        department.setId(departmentId);
        entity.setDepartment(department);
    }

    @Test
    void createSavesAndMapsResult() {
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(entity.getDepartment()));
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
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(entity.getDepartment()));
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
        verify(repository, never()).delete(any(CityEntity.class));
    }

    @Test
    void listPassesPageableAndMapsPage() {
        var pageable = PageRequest.of(1, 2, Sort.by("name"));
        when(repository.findAll(ArgumentMatchers.<Specification<CityEntity>>any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(entity), pageable, 3));
        when(mapper.toResponse(entity)).thenReturn(response);
        var result = service.findAll(departmentId, pageable);
        assertEquals(List.of(response), result.getContent());
        assertEquals(3, result.getTotalElements());
        assertEquals(1, result.getNumber());
        verify(repository).findAll(ArgumentMatchers.<Specification<CityEntity>>any(), eq(pageable));
    }
    @Test
    void missingDepartmentDoesNotSave() {
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.create(request));
        verify(repository, never()).save(any());
        verifyNoInteractions(mapper);
    }

    @Test
    void updateWithMissingDepartmentDoesNotSave() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.update(id, request));
        verify(repository, never()).save(any());
        verifyNoInteractions(mapper);
    }
}
