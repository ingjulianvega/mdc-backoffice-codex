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
class SupplierServiceImplTest {
    @Mock SupplierRepository repository;

    final UUID id = UUID.randomUUID();
    final SupplierRequestDTO request = new SupplierRequestDTO("Wax company", "Ana", "supplier@example.com", "3001234567", "900123");
    SupplierServiceImpl service;
    SupplierEntity entity;

    @BeforeEach
    void setUp() {
        var mapper = Mappers.getMapper(SupplierMapper.class);
        service = new SupplierServiceImpl(repository, mapper);
        entity = mapper.toEntity(request);
        entity.setId(id);
        entity.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        entity.setCreatedBy("original");

    }


    @Test
    void createMapsAndSaves() {
        when(repository.save(any(SupplierEntity.class))).thenAnswer(invocation -> {
            SupplierEntity saved = invocation.getArgument(0);
            assertNull(saved.getId());
            assertNull(saved.getCreatedAt());

            saved.setId(id);
            return saved;
        });
        var result = service.create(request);
        assertEquals(id, result.id());
        assertEquals(request.companyName(), result.companyName());
        assertEquals(request.contactName(), result.contactName());
        assertEquals(request.email(), result.email());
        assertEquals(request.phone(), result.phone());
        assertEquals(request.taxId(), result.taxId());


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
        verify(repository, never()).delete(any(SupplierEntity.class));
    }

    @Test
    void updatePreservesIdentityAndAudit() {
        entity.setCompanyName("Old value");
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        
        var result = service.update(id, request);
        assertEquals(id, result.id());
        assertEquals(entity.getCreatedAt(), result.createdAt());
        assertEquals("original", result.createdBy());

        assertEquals(request.companyName(), result.companyName());

        verify(repository).flush();
        verify(repository).existsByEmailIgnoreCaseAndIdNot(request.email(), id);
        verify(repository).existsByTaxIdAndIdNot(request.taxId(), id);
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
        when(repository.findAll(ArgumentMatchers.<Specification<SupplierEntity>>any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(entity), pageable, 3));
        var result = service.findAll("a", "a", pageable);
        assertEquals(id, result.getContent().getFirst().id());
        assertEquals(3, result.getTotalElements());
        assertEquals(1, result.getNumber());
    }

    @Test
    void emptyUnfilteredPage() {
        var pageable = PageRequest.of(0, 20);
        when(repository.findAll(ArgumentMatchers.<Specification<SupplierEntity>>any(), eq(pageable)))
                .thenReturn(Page.empty(pageable));
        assertTrue(service.findAll(null, null, pageable).isEmpty());
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
        assertEquals(request.companyName(), entity.getCompanyName());
    }

    @Test
    void duplicateTaxIdIsRejectedOnCreate() {
        when(repository.existsByTaxId(request.taxId())).thenReturn(true);
        assertThrows(com.mdc.backoffice.exception.DuplicateResourceException.class, () -> service.create(request));
        verify(repository, never()).save(any());
    }
    @Test
    void duplicateTaxIdIsRejectedOnUpdate() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.existsByTaxIdAndIdNot(request.taxId(), id)).thenReturn(true);
        assertThrows(com.mdc.backoffice.exception.DuplicateResourceException.class, () -> service.update(id, request));
        verify(repository, never()).save(any());
        assertEquals(request.companyName(), entity.getCompanyName());
    }
}
