package com.mdc.backoffice.service.impl;

import com.mdc.backoffice.exception.ResourceNotFoundException;
import com.mdc.backoffice.mapper.*;
import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.model.entity.*;
import com.mdc.backoffice.repository.*;
import java.util.*;
import java.time.Instant;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {
    @Mock CustomerRepository repository;

    final UUID id = UUID.randomUUID();
    final CustomerRequestDTO request = new CustomerRequestDTO("CC", "123", "Ana", "Perez", "3001234567", "ana@example.com");
    CustomerServiceImpl service;
    CustomerEntity entity;

    @BeforeEach
    void setUp() {
        var mapper = Mappers.getMapper(CustomerMapper.class);
        service = new CustomerServiceImpl(repository, mapper);
        entity = mapper.toEntity(request);
        entity.setId(id);
        entity.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        entity.setCreatedBy("original");

    }


    @Test
    void createMapsAndSaves() {
        when(repository.save(any(CustomerEntity.class))).thenAnswer(invocation -> {
            CustomerEntity saved = invocation.getArgument(0);
            assertNull(saved.getId());
            assertNull(saved.getCreatedAt());

            saved.setId(id);
            return saved;
        });
        var result = service.create(request);
        assertEquals(id, result.id());
        assertEquals(request.documentType(), result.documentType());
        assertEquals(request.documentNumber(), result.documentNumber());
        assertEquals(request.firstName(), result.firstName());
        assertEquals(request.lastName(), result.lastName());
        assertEquals(request.phoneNumber(), result.phoneNumber());
        assertEquals(request.email(), result.email());


    }

    @Test
    void findByIdMapsResult() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        assertEquals(id, service.findById(id).id());
    }

    @Test
    void missingIdThrowsForReadUpdateAndDelete() {
        assertThrows(ResourceNotFoundException.class, () -> service.findById(id));
        assertThrows(ResourceNotFoundException.class, () -> service.update(id, request));
        assertThrows(ResourceNotFoundException.class, () -> service.delete(id));
        verify(repository, never()).save(any());
        verify(repository, never()).delete(any(CustomerEntity.class));
    }

    @Test
    void updatePreservesIdentityAndAudit() {
        entity.setFirstName("Old value");
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        
        var result = service.update(id, request);
        assertEquals(id, result.id());
        assertEquals(entity.getCreatedAt(), result.createdAt());
        assertEquals("original", result.createdBy());

        assertEquals(request.documentType(), result.documentType());

        verify(repository).flush();
        verify(repository).existsByDocumentNumberAndIdNot(request.documentNumber(), id);
        verify(repository).existsByEmailIgnoreCaseAndIdNot(request.email(), id);
    }

    @Test
    void deleteExisting() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        service.delete(id);
        verify(repository).delete(entity);
    }

    @Test
    void listPreservesPaginationAndMapsContent() {
        var pageable = PageRequest.of(1, 2);
        when(repository.findAll(ArgumentMatchers.<Specification<CustomerEntity>>any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(entity), pageable, 3));
        var result = service.findAll("a", "a", "a", pageable);
        assertEquals(id, result.getContent().getFirst().id());
        assertEquals(3, result.getTotalElements());
        assertEquals(1, result.getNumber());
    }

    @Test
    void emptyUnfilteredPage() {
        var pageable = PageRequest.of(0, 20);
        when(repository.findAll(ArgumentMatchers.<Specification<CustomerEntity>>any(), eq(pageable)))
                .thenReturn(Page.empty(pageable));
        assertTrue(service.findAll(null, null, null, pageable).isEmpty());
    }

    @Test
    void duplicateDocumentNumberIsRejectedOnCreate() {
        when(repository.existsByDocumentNumber(request.documentNumber())).thenReturn(true);
        assertThrows(com.mdc.backoffice.exception.DuplicateResourceException.class, () -> service.create(request));
        verify(repository, never()).save(any());
    }
    @Test
    void duplicateDocumentNumberIsRejectedOnUpdate() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.existsByDocumentNumberAndIdNot(request.documentNumber(), id)).thenReturn(true);
        assertThrows(com.mdc.backoffice.exception.DuplicateResourceException.class, () -> service.update(id, request));
        verify(repository, never()).save(any());
        assertEquals(request.documentType(), entity.getDocumentType());
    }

    @Test
    void duplicateEmailIsRejectedOnCreate() {
        when(repository.existsByEmailIgnoreCase(request.email())).thenReturn(true);
        assertThrows(com.mdc.backoffice.exception.DuplicateResourceException.class, () -> service.create(request));
        verify(repository, never()).save(any());
    }
    @Test
    void duplicateEmailIsRejectedOnUpdate() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.existsByEmailIgnoreCaseAndIdNot(request.email(), id)).thenReturn(true);
        assertThrows(com.mdc.backoffice.exception.DuplicateResourceException.class, () -> service.update(id, request));
        verify(repository, never()).save(any());
        assertEquals(request.documentType(), entity.getDocumentType());
    }
}
