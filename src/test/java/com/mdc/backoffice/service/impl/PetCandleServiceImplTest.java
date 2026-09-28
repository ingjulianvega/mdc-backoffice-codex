package com.mdc.backoffice.service.impl;

import com.mdc.backoffice.exception.*;
import com.mdc.backoffice.mapper.*;
import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.model.entity.*;
import com.mdc.backoffice.model.enum_.*;
import com.mdc.backoffice.repository.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mapstruct.factory.Mappers;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class PetCandleServiceImplTest {
    @Mock PetCandleRepository repository;
    @Mock OrderItemRepository items;
    @Mock OrderRepository orders;
    @Mock MaterialRepository materials;
    PetCandleServiceImpl service;
    PetCandleEntity candle;
    OrderItemEntity item;
    OrderEntity order;
    UUID id = UUID.randomUUID();

    @BeforeEach void setup() {
        var mapper = Mappers.getMapper(PetCandleMapper.class);
        org.springframework.test.util.ReflectionTestUtils.setField(mapper, "petCandlePhotoMapper",
                Mappers.getMapper(PetCandlePhotoMapper.class));
        service = new PetCandleServiceImpl(repository, items, orders, materials, mapper);
        order = new OrderEntity(); order.setId(UUID.randomUUID()); order.setStatus(OrderStatus.PENDING);
        item = new OrderItemEntity(); item.setId(UUID.randomUUID()); item.setOrder(order);
        order.getItems().add(item);
        candle = new PetCandleEntity(); candle.setId(id); candle.setOrderItem(item); candle.setPetName("");
    }

    void stubPatch() {
        when(repository.findOrderIdById(id)).thenReturn(Optional.of(order.getId()));
        when(orders.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));
        when(repository.findById(id)).thenReturn(Optional.of(candle));
    }

    PetCandleStatusUpdateDTO patch(String... s) {
        return new PetCandleStatusUpdateDTO(s[0], s[1], s[2], s[3], s[4], s[5], s[6], s[7]);
    }

    String[] finals() {
        return new String[]{"COMPLETED", "AT_WORKSHOP_ONE", "AT_WORKSHOP_ONE", "ASSEMBLED",
                "AVAILABLE", "AT_WORKSHOP_ONE", "PACKAGED", "DELIVERED"};
    }

    @Test void createsAllPendingAndNonNullInitialName() {
        when(items.findById(item.getId())).thenReturn(Optional.of(item));
        when(orders.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        var result = service.create(new PetCandleCreateDTO(item.getId()));
        assertEquals(item.getId(), result.orderItemId());
        assertEquals("", result.petName());
        assertEquals(List.of("PENDING", "PENDING", "PENDING", "PENDING", "PENDING", "PENDING", "PENDING", "PENDING"),
                List.of(result.baseStatus(), result.headStatus(), result.labelStatus(), result.assemblyStatus(),
                        result.boxStatus(), result.shippingGuideStatus(), result.packagingStatus(), result.carrierStatus()));
        assertTrue(result.photos().isEmpty());
        verifyNoInteractions(materials);
    }

    @Test void rejectsMissingItemAndDuplicateCandle() {
        assertThrows(ResourceNotFoundException.class, () -> service.create(new PetCandleCreateDTO(item.getId())));
        when(items.findById(item.getId())).thenReturn(Optional.of(item));
        when(orders.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));
        when(repository.findByOrderItemId(item.getId())).thenReturn(Optional.of(candle));
        assertThrows(DuplicateResourceException.class, () -> service.create(new PetCandleCreateDTO(item.getId())));
        verify(repository, never()).save(any());
    }

    @Test void updatesPersonalizationAndClearsNullableFieldsWithoutChangingStates() {
        stubPatch();
        var aroma = new MaterialEntity(); aroma.setId(UUID.randomUUID());
        when(materials.findById(aroma.getId())).thenReturn(Optional.of(aroma));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        var result = service.update(id, new PetCandleUpdateDTO("Luna", "Round", aroma.getId(), "Love", "Dog", "Mix"));
        assertEquals("Luna", result.petName()); assertEquals("Round", result.labelType());
        assertEquals(aroma.getId(), result.aromaMaterialId()); assertEquals("Love", result.customMessage());
        assertEquals("Dog", result.specie()); assertEquals("Mix", result.breed());
        assertEquals("PENDING", result.baseStatus());
        result = service.update(id, new PetCandleUpdateDTO("Sol", null, null, null, null, null));
        assertNull(result.aromaMaterialId()); assertNull(result.labelType()); assertNull(result.customMessage());
        assertNull(result.specie()); assertNull(result.breed());
    }

    @Test void missingAromaDoesNotMutateCandle() {
        stubPatch();
        assertThrows(ResourceNotFoundException.class, () -> service.update(id,
                new PetCandleUpdateDTO("Luna", null, UUID.randomUUID(), null, null, null)));
        assertEquals("", candle.getPetName()); verify(repository, never()).save(any());
    }

    @Test void completeCandleCompletesItsItemAndOrder() {
        stubPatch();
        var sibling = new OrderItemEntity(); sibling.setStatus(OrderItemStatus.COMPLETED);
        order.getItems().add(sibling);
        var result = service.updateStatuses(id, patch(finals()));
        assertEquals("DELIVERED", result.carrierStatus());
        assertEquals(OrderItemStatus.COMPLETED, item.getStatus());
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        verify(items).save(item); verify(orders).save(order);
        var sequence = inOrder(orders, repository);
        sequence.verify(orders).findByIdForUpdate(order.getId());
        sequence.verify(repository).findById(id);
    }

    @Test void pendingSiblingPreventsOrderCompletion() {
        stubPatch(); order.getItems().add(new OrderItemEntity());
        service.updateStatuses(id, patch(finals()));
        assertEquals(OrderItemStatus.COMPLETED, item.getStatus());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        verify(orders, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7})
    void everyComponentIsRequiredForCompletion(int index) {
        stubPatch(); var values = finals(); values[index] = "PENDING";
        service.updateStatuses(id, patch(values));
        assertEquals(OrderItemStatus.PENDING, item.getStatus());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        verify(items, never()).save(any()); verify(orders, never()).save(any());
    }

    @Test void partialPatchPreservesOtherStatesAndEmptyPatchIsNoOp() {
        stubPatch(); candle.setHeadStatus(HeadStatus.PAINTED);
        service.updateStatuses(id, patch("COMPLETED", null, null, null, null, null, null, null));
        var result = service.updateStatuses(id, patch(new String[8]));
        assertEquals("COMPLETED", result.baseStatus()); assertEquals("PAINTED", result.headStatus());
        assertEquals("PENDING", result.carrierStatus());
        verify(items, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7})
    void invalidComponentRejectsWholePatchBeforeMutation(int index) {
        var values = finals(); values[index] = "INVALID";
        assertThrows(InvalidPetCandleException.class, () -> service.updateStatuses(id, patch(values)));
        assertEquals(BaseStatus.PENDING, candle.getBaseStatus());
        verifyNoInteractions(repository, items, orders);
    }

    @Test void wrongComponentValueAndBlankAreInvalid() {
        assertThrows(InvalidPetCandleException.class, () -> service.updateStatuses(id,
                patch("ASSEMBLED", null, null, null, null, null, null, null)));
        assertThrows(InvalidPetCandleException.class, () -> service.updateStatuses(id,
                patch(null, "", null, null, null, null, null, null)));
    }

    @Test void readsCandleWithPhotosAndByOrderItem() {
        var photo = new PetCandlePhotoEntity(); photo.setId(UUID.randomUUID());
        photo.setPetCandle(candle); photo.setPhotoUrl("https://example.com/luna.jpg"); photo.setIsPrimary(true);
        candle.getPhotos().add(photo);
        when(repository.findById(id)).thenReturn(Optional.of(candle));
        when(repository.findByOrderItemId(item.getId())).thenReturn(Optional.of(candle));
        var result = service.findById(id);
        assertEquals(id, result.photos().getFirst().petCandleId());
        assertTrue(result.photos().getFirst().isPrimary());
        assertEquals(result, service.findByOrderItemId(item.getId()));
    }

    @Test void missingCandleReturnsNotFoundAcrossOperations() {
        assertThrows(ResourceNotFoundException.class, () -> service.findById(id));
        assertThrows(ResourceNotFoundException.class, () -> service.findByOrderItemId(id));
        assertThrows(ResourceNotFoundException.class, () -> service.update(id,
                new PetCandleUpdateDTO("Luna", null, null, null, null, null)));
        assertThrows(ResourceNotFoundException.class, () -> service.updateStatuses(id, patch(finals())));
    }
}
