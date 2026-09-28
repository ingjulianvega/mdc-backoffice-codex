package com.mdc.backoffice.service.impl;

import com.mdc.backoffice.exception.*;
import com.mdc.backoffice.mapper.*;
import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.model.entity.*;
import com.mdc.backoffice.repository.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaterialPurchaseServiceImplTest {
    @Mock MaterialPurchaseRepository repository;
    @Mock SupplierRepository suppliers;
    @Mock MaterialRepository materials;
    MaterialPurchaseServiceImpl service;
    final UUID supplierId = UUID.randomUUID();
    final UUID materialId = UUID.randomUUID();
    final UUID id = UUID.randomUUID();
    final Instant date = Instant.parse("2026-09-27T12:00:00Z");
    MaterialEntity material;

    @BeforeEach
    void setUp() {
        var mapper = Mappers.getMapper(MaterialPurchaseMapper.class);
        var itemMapper = Mappers.getMapper(MaterialPurchaseItemMapper.class);
        ReflectionTestUtils.setField(mapper, "supplierMapper", Mappers.getMapper(SupplierMapper.class));
        ReflectionTestUtils.setField(mapper, "materialPurchaseItemMapper", itemMapper);
        service = new MaterialPurchaseServiceImpl(repository, suppliers, materials, mapper, itemMapper);
        material = new MaterialEntity();
        material.setId(materialId);
        material.setName("Wax");
        material.setCurrentStock(new BigDecimal("10.0000"));
    }

    MaterialPurchaseRequestDTO request() {
        return new MaterialPurchaseRequestDTO(supplierId, date, List.of(
                new MaterialPurchaseItemRequestDTO(materialId, 2.5, 3.2),
                new MaterialPurchaseItemRequestDTO(materialId, 0.1, 0.2)));
    }

    void supplierExists() {
        var supplier = new SupplierEntity();
        supplier.setId(supplierId);
        when(suppliers.findById(supplierId)).thenReturn(Optional.of(supplier));
    }

    @Test
    void calculatesDecimalTotalsAccumulatesRepeatedMaterialsAndMapsResponse() {
        supplierExists();
        when(materials.findByIdForUpdate(materialId)).thenReturn(Optional.of(material));
        when(repository.save(any())).thenAnswer(invocation -> {
            MaterialPurchaseEntity entity = invocation.getArgument(0);
            entity.setId(id);
            entity.setCreatedAt(date);
            entity.getItems().forEach(item -> item.setId(UUID.randomUUID()));
            return entity;
        });
        var response = service.create(request());
        assertEquals(8.02, response.totalCost());
        assertEquals(8.0, response.items().getFirst().subtotal());
        assertEquals(0.02, response.items().get(1).subtotal());
        assertEquals(materialId, response.items().getFirst().materialId());
        assertEquals("Wax", response.items().getFirst().materialName());
        assertEquals(supplierId, response.supplier().id());
        assertEquals(date, response.purchaseDate());
        assertEquals(date, response.createdAt());
        assertEquals(new BigDecimal("12.6000"), material.getCurrentStock());
        verify(materials).save(material);
        verify(materials).findByIdForUpdate(materialId);
        var captor = ArgumentCaptor.forClass(MaterialPurchaseEntity.class);
        verify(repository).save(captor.capture());
        captor.getValue().getItems().forEach(item -> assertSame(captor.getValue(), item.getMaterialPurchase()));
    }

    @Test
    void defaultsMissingPurchaseDateAndUpdatesMultipleMaterials() {
        supplierExists();
        var second = new MaterialEntity();
        second.setId(UUID.randomUUID());
        second.setCurrentStock(BigDecimal.ZERO);
        when(materials.findByIdForUpdate(materialId)).thenReturn(Optional.of(material));
        when(materials.findByIdForUpdate(second.getId())).thenReturn(Optional.of(second));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var before = Instant.now();
        var response = service.create(new MaterialPurchaseRequestDTO(supplierId, null, List.of(
                new MaterialPurchaseItemRequestDTO(materialId, 1.0, 2.0),
                new MaterialPurchaseItemRequestDTO(second.getId(), 3.0, 4.0))));
        assertFalse(response.purchaseDate().isBefore(before));
        assertFalse(response.purchaseDate().isAfter(Instant.now()));
        assertEquals(14.0, response.totalCost());
        assertEquals(0, second.getCurrentStock().compareTo(new BigDecimal("3")));
        verify(materials).save(material);
        verify(materials).save(second);
    }

    @Test
    void missingSupplierDoesNotWrite() {
        assertThrows(ResourceNotFoundException.class, () -> service.create(request()));
        verifyNoInteractions(materials, repository);
    }

    @Test
    void missingMaterialDoesNotWrite() {
        supplierExists();
        assertThrows(ResourceNotFoundException.class, () -> service.create(request()));
        verify(materials, never()).save(any());
        verifyNoInteractions(repository);
    }

    MaterialPurchaseEntity purchase() {
        var purchase = new MaterialPurchaseEntity();
        purchase.setId(id);
        for (String quantity : List.of("2.5", "0.1")) {
            var item = new MaterialPurchaseItemEntity();
            item.setMaterial(material);
            item.setQuantity(new BigDecimal(quantity));
            purchase.getItems().add(item);
        }
        return purchase;
    }

    @Test
    void deleteReversesAllQuantitiesIncludingRepeatedMaterials() {
        var purchase = purchase();
        when(repository.findByIdForUpdate(id)).thenReturn(Optional.of(purchase));
        when(materials.findByIdForUpdate(materialId)).thenReturn(Optional.of(material));
        service.delete(id);
        assertEquals(new BigDecimal("7.4000"), material.getCurrentStock());
        verify(materials).save(material);
        verify(repository).delete(purchase);
    }

    @Test
    void insufficientStockPreventsDeletionAndWrites() {
        material.setCurrentStock(new BigDecimal("2.5"));
        when(repository.findByIdForUpdate(id)).thenReturn(Optional.of(purchase()));
        when(materials.findByIdForUpdate(materialId)).thenReturn(Optional.of(material));
        assertThrows(InvalidMaterialPurchaseException.class, () -> service.delete(id));
        assertEquals(new BigDecimal("2.5"), material.getCurrentStock());
        verify(materials, never()).save(any());
        verify(repository, never()).delete(any(MaterialPurchaseEntity.class));
    }

    @Test
    void rejectsStockOverflowBeforeSaving() {
        supplierExists();
        material.setCurrentStock(new BigDecimal("99999999"));
        when(materials.findByIdForUpdate(materialId)).thenReturn(Optional.of(material));
        assertThrows(InvalidMaterialPurchaseException.class, () -> service.create(request()));
        verify(materials, never()).save(any());
        verifyNoInteractions(repository);
    }

    @Test
    void missingPurchaseFailsReadAndDelete() {
        assertThrows(ResourceNotFoundException.class, () -> service.findById(id));
        assertThrows(ResourceNotFoundException.class, () -> service.delete(id));
        verifyNoInteractions(materials);
    }

    @Test
    void findByIdMapsItems() {
        when(repository.findById(id)).thenReturn(Optional.of(purchase()));
        var response = service.findById(id);
        assertEquals(id, response.id());
        assertEquals(2, response.items().size());
    }

    @Test
    void listsWithSpecificationAndPagination() {
        var pageable = PageRequest.of(0, 5);
        when(repository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(purchase()), pageable, 1));
        var response = service.findAll(supplierId, date, date, pageable);
        assertEquals(id, response.getContent().getFirst().id());
        assertEquals(1, response.getTotalElements());
    }

    @Test
    void rejectsReversedDateRange() {
        assertThrows(InvalidMaterialPurchaseException.class,
                () -> service.findAll(null, date.plusSeconds(1), date, Pageable.unpaged()));
        verifyNoInteractions(repository);
    }

    @Test
    void persistenceFailuresPropagateToTransactionalBoundary() {
        supplierExists();
        when(materials.findByIdForUpdate(materialId)).thenReturn(Optional.of(material));
        when(materials.save(material)).thenThrow(new IllegalStateException("stock save failed"));
        assertThrows(IllegalStateException.class, () -> service.create(request()));
        verifyNoInteractions(repository);
    }
}
