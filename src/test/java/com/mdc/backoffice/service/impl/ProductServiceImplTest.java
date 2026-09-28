package com.mdc.backoffice.service.impl;

import com.mdc.backoffice.exception.ResourceNotFoundException;
import com.mdc.backoffice.mapper.*;
import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.model.entity.*;
import com.mdc.backoffice.repository.*;
import java.util.*;
import java.math.BigDecimal;
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
class ProductServiceImplTest {
    @Mock ProductRepository repository;

    final UUID id = UUID.randomUUID();
    final UUID productId = UUID.randomUUID();
    final UUID materialId = UUID.randomUUID();
    final ProductRequestDTO request = new ProductRequestDTO("Candle", null, 15000L, 0, null);
    ProductServiceImpl service;
    ProductEntity entity;

    @BeforeEach
    void setUp() {
        var mapper = Mappers.getMapper(ProductMapper.class);
        service = new ProductServiceImpl(repository, mapper);
        entity = mapper.toEntity(request);
        entity.setId(id);
        entity.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        entity.setCreatedBy("original");

    }


    @Test
    void createMapsAndSaves() {
        when(repository.save(any(ProductEntity.class))).thenAnswer(invocation -> {
            ProductEntity saved = invocation.getArgument(0);
            assertNull(saved.getId());
            assertNull(saved.getCreatedAt());

            saved.setId(id);
            return saved;
        });
        var result = service.create(request);
        assertEquals(id, result.id());
        assertEquals(request.price(), result.price());
        assertEquals(0, result.stockQuantity());
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
        verify(repository, never()).delete(any(ProductEntity.class));
    }

    @Test
    void updatePreservesIdentityAndAudit() {
        entity.setName("Old candle");
        entity.setPrice(1L);
        entity.setStockQuantity(10);
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        entity.setDescription("Old description");
        entity.setMinStockAlert(5);
        var result = service.update(id, request);
        assertEquals(id, result.id());
        assertEquals(entity.getCreatedAt(), result.createdAt());
        assertEquals("original", result.createdBy());
        assertNull(result.description());
        assertNull(result.minStockAlert());
        assertEquals(request.name(), result.name());
        assertEquals(request.price(), result.price());
        assertEquals(request.stockQuantity(), result.stockQuantity());
        verify(repository).flush();
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
        when(repository.findAll(ArgumentMatchers.<Specification<ProductEntity>>any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(entity), pageable, 3));
        var result = service.findAll("a", pageable);
        assertEquals(id, result.getContent().getFirst().id());
        assertEquals(3, result.getTotalElements());
        assertEquals(1, result.getNumber());
    }

    @Test
    void emptyUnfilteredPage() {
        var pageable = PageRequest.of(0, 20);
        when(repository.findAll(ArgumentMatchers.<Specification<ProductEntity>>any(), eq(pageable)))
                .thenReturn(Page.empty(pageable));
        assertTrue(service.findAll(null, pageable).isEmpty());
    }
}
