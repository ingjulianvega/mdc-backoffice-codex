package com.mdc.backoffice.service.impl;

import com.mdc.backoffice.exception.*;
import com.mdc.backoffice.mapper.*;
import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.model.entity.*;
import com.mdc.backoffice.model.enum_.OrderStatus;
import com.mdc.backoffice.repository.*;
import com.mdc.backoffice.service.OrderService;
import com.mdc.backoffice.specification.OrderSpecification;
import java.time.Instant;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository repository;
    private final CustomerRepository customers;
    private final ProductRepository products;
    private final AddressRepository addresses;
    private final OrderMapper mapper;
    private final OrderItemMapper itemMapper;

    @Override
    @Transactional
    public OrderResponseDTO create(OrderRequestDTO request) {
        var customer = customers.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.customerId()));
        var order = mapper.toEntity(request);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);
        var locked = lockProducts(request.items().stream().map(OrderItemRequestDTO::productId).toList());
        var quantities = new LinkedHashMap<UUID, Long>();
        long total = 0;
        try {
            for (var line : request.items()) {
                var item = itemMapper.toEntity(line);
                item.setOrder(order);
                item.setProduct(locked.get(line.productId()));
                if (line.addressId() != null) {
                    item.setAddress(addresses.findById(line.addressId())
                            .orElseThrow(() -> new ResourceNotFoundException("Address", line.addressId())));
                }
                item.setSubtotal(Math.multiplyExact(line.quantity().longValue(), line.unitPrice()));
                total = Math.addExact(total, item.getSubtotal());
                quantities.merge(line.productId(), line.quantity().longValue(), Math::addExact);
                order.getItems().add(item);
            }
        } catch (ArithmeticException exception) {
            throw new InvalidOrderException("Order amount exceeds the supported range.");
        }
        order.setTotalAmount(total);
        updateStock(locked, quantities, false);
        return mapper.toResponse(repository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> findAll(UUID customerId, Instant startDate, Instant endDate, Pageable pageable) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new InvalidOrderException("startDate must not be after endDate.");
        }
        return repository.findAll(OrderSpecification.hasCustomer(customerId)
                .and(OrderSpecification.orderedFrom(startDate))
                .and(OrderSpecification.orderedUntil(endDate)), pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO findById(UUID id) {
        return mapper.toResponse(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id)));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        var order = repository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        var quantities = new LinkedHashMap<UUID, Long>();
        order.getItems().forEach(item -> quantities.merge(
                item.getProduct().getId(), item.getQuantity().longValue(), Math::addExact));
        updateStock(lockProducts(quantities.keySet()), quantities, true);
        repository.delete(order);
    }

    private Map<UUID, ProductEntity> lockProducts(Collection<UUID> ids) {
        var locked = new LinkedHashMap<UUID, ProductEntity>();
        // Stable locking order avoids deadlocks between orders sharing products.
        ids.stream().distinct().sorted().forEach(id -> locked.put(id,
                products.findByIdForUpdate(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Product", id))));
        return locked;
    }

    private void updateStock(Map<UUID, ProductEntity> locked, Map<UUID, Long> quantities, boolean restore) {
        var stocks = new LinkedHashMap<UUID, Integer>();
        quantities.forEach((id, quantity) -> {
            long stock = locked.get(id).getStockQuantity().longValue() + (restore ? quantity : -quantity);
            if (stock < 0 || stock > Integer.MAX_VALUE) {
                throw new InvalidOrderException("Insufficient stock or stock out of range for product: " + id);
            }
            stocks.put(id, (int) stock);
        });
        stocks.forEach((id, stock) -> {
            var product = locked.get(id);
            product.setStockQuantity(stock);
            products.save(product);
        });
    }
}
