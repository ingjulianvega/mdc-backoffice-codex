package com.mdc.backoffice.service.impl;

import com.mdc.backoffice.exception.*;
import com.mdc.backoffice.mapper.*;
import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.model.entity.*;
import com.mdc.backoffice.model.enum_.OrderStatus;
import com.mdc.backoffice.repository.*;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {
    @Mock OrderRepository repository;
    @Mock CustomerRepository customers;
    @Mock ProductRepository products;
    @Mock AddressRepository addresses;
    OrderServiceImpl service;
    UUID customerId = UUID.randomUUID(), productId = UUID.randomUUID(), id = UUID.randomUUID();
    Instant date = Instant.parse("2026-09-28T12:00:00Z");
    ProductEntity product;

    @BeforeEach void setup() {
        var mapper = Mappers.getMapper(OrderMapper.class);
        var items = Mappers.getMapper(OrderItemMapper.class);
        ReflectionTestUtils.setField(mapper, "customerMapper", Mappers.getMapper(CustomerMapper.class));
        ReflectionTestUtils.setField(mapper, "orderItemMapper", items);
        service = new OrderServiceImpl(repository, customers, products, addresses, mapper, items);
        product = new ProductEntity();
        product.setId(productId);
        product.setName("Candle");
        product.setStockQuantity(10);
    }
    OrderRequestDTO request() {
        return new OrderRequestDTO(customerId, date, List.of(
                new OrderItemRequestDTO(productId, 2, 3000000000L, null, "TRACK", "Carrier"),
                new OrderItemRequestDTO(productId, 3, 4L, null, null, null)));
    }
    void references() {
        var customer = new CustomerEntity(); customer.setId(customerId);
        when(customers.findById(customerId)).thenReturn(Optional.of(customer));
        when(products.findByIdForUpdate(productId)).thenReturn(Optional.of(product));
    }
    @Test void calculatesLongTotalsAndAggregatesRepeatedProducts() {
        references();
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        var result = service.create(request());
        assertEquals(6000000012L, result.totalAmount());
        assertEquals("PENDING", result.status());
        assertEquals(6000000000L, result.items().getFirst().subtotal());
        assertEquals("Candle", result.items().getFirst().productName());
        assertEquals("TRACK", result.items().getFirst().trackingNumber());
        assertEquals("Carrier", result.items().getFirst().carrierName());
        assertEquals(customerId, result.customer().id());
        assertEquals(5, product.getStockQuantity());
        verify(products).findByIdForUpdate(productId);
        var captor = ArgumentCaptor.forClass(OrderEntity.class);
        verify(repository).save(captor.capture());
        assertEquals(OrderStatus.PENDING, captor.getValue().getStatus());
        captor.getValue().getItems().forEach(item -> assertSame(captor.getValue(), item.getOrder()));
    }
    @Test void insufficientCombinedStockDoesNotWrite() {
        references(); product.setStockQuantity(4);
        assertThrows(InvalidOrderException.class, () -> service.create(request()));
        assertEquals(4, product.getStockQuantity());
        verify(products, never()).save(any()); verifyNoInteractions(repository);
    }
    @Test void amountOverflowDoesNotWrite() {
        references();
        assertThrows(InvalidOrderException.class, () -> service.create(new OrderRequestDTO(customerId, date,
                List.of(new OrderItemRequestDTO(productId, 2, Long.MAX_VALUE, null, null, null)))));
        verify(products, never()).save(any()); verifyNoInteractions(repository);
    }
    OrderEntity order() {
        var order = new OrderEntity(); order.setId(id);
        for (int quantity : List.of(2, 3)) {
            var item = new OrderItemEntity(); item.setProduct(product); item.setQuantity(quantity);
            order.getItems().add(item);
        }
        return order;
    }
    @Test void deletionRestoresRepeatedProducts() {
        var order = order();
        when(repository.findByIdForUpdate(id)).thenReturn(Optional.of(order));
        when(products.findByIdForUpdate(productId)).thenReturn(Optional.of(product));
        service.delete(id);
        assertEquals(15, product.getStockQuantity());
        verify(products).save(product); verify(repository).delete(order);
    }
    @Test void restorationOverflowPreventsDeletion() {
        product.setStockQuantity(Integer.MAX_VALUE);
        when(repository.findByIdForUpdate(id)).thenReturn(Optional.of(order()));
        when(products.findByIdForUpdate(productId)).thenReturn(Optional.of(product));
        assertThrows(InvalidOrderException.class, () -> service.delete(id));
        verify(repository, never()).delete(any(OrderEntity.class));
        verify(products, never()).save(any());
    }
    @Test void missingCustomer() {
        assertThrows(ResourceNotFoundException.class, () -> service.create(request()));
        verifyNoInteractions(products, repository);
    }
    @Test void missingProduct() {
        when(customers.findById(customerId)).thenReturn(Optional.of(new CustomerEntity()));
        assertThrows(ResourceNotFoundException.class, () -> service.create(request()));
        verifyNoInteractions(repository);
    }
    @Test void missingOrder() {
        assertThrows(ResourceNotFoundException.class, () -> service.findById(id));
        assertThrows(ResourceNotFoundException.class, () -> service.delete(id));
        verifyNoInteractions(products);
    }
    @Test void invalidDateRange() {
        assertThrows(InvalidOrderException.class, () -> service.findAll(null, date.plusSeconds(1), date, Pageable.unpaged()));
        verifyNoInteractions(repository);
    }
    @Test void addressIsValidatedAndMapped() {
        references(); var address = new AddressEntity(); address.setId(UUID.randomUUID());
        when(addresses.findById(address.getId())).thenReturn(Optional.of(address));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        var result = service.create(new OrderRequestDTO(customerId, date, List.of(
                new OrderItemRequestDTO(productId, 1, 1L, address.getId(), null, null))));
        assertEquals(address.getId(), result.items().getFirst().addressId());
    }
}
