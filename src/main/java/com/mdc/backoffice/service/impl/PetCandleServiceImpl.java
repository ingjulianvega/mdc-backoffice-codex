package com.mdc.backoffice.service.impl;

import com.mdc.backoffice.exception.*;
import com.mdc.backoffice.mapper.PetCandleMapper;
import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.model.entity.*;
import com.mdc.backoffice.model.enum_.*;
import com.mdc.backoffice.repository.*;
import com.mdc.backoffice.service.PetCandleService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
public class PetCandleServiceImpl implements PetCandleService {
    private final PetCandleRepository repository;
    private final OrderItemRepository items;
    private final OrderRepository orders;
    private final MaterialRepository materials;
    private final PetCandleMapper mapper;

    @Override
    public PetCandleResponseDTO create(PetCandleCreateDTO request) {
        var item = items.findById(request.orderItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Order item", request.orderItemId()));
        lockOrder(item.getOrder().getId());
        if (repository.findByOrderItemId(item.getId()).isPresent()) {
            throw new DuplicateResourceException("A pet candle already exists for order item: " + item.getId());
        }
        var candle = new PetCandleEntity();
        candle.setOrderItem(item);
        // The initial contract only supplies orderItemId; pet_name is NOT NULL.
        candle.setPetName("");
        return mapper.toResponse(repository.save(candle));
    }

    @Override
    @Transactional(readOnly = true)
    public PetCandleResponseDTO findById(UUID id) {
        return mapper.toResponse(require(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PetCandleResponseDTO findByOrderItemId(UUID orderItemId) {
        return mapper.toResponse(repository.findByOrderItemId(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Pet candle for order item", orderItemId)));
    }

    @Override
    public PetCandleResponseDTO update(UUID id, PetCandleUpdateDTO request) {
        var orderId = repository.findOrderIdById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pet candle", id));
        lockOrder(orderId);
        var candle = require(id);
        var aroma = request.aromaMaterialId() == null ? null : materials.findById(request.aromaMaterialId())
                .orElseThrow(() -> new ResourceNotFoundException("Material", request.aromaMaterialId()));
        mapper.update(request, candle);
        candle.setAromaMaterial(aroma);
        return mapper.toResponse(repository.save(candle));
    }

    @Override
    public PetCandleResponseDTO updateStatuses(UUID id, PetCandleStatusUpdateDTO request) {
        // Parse the entire patch before mutating any managed entity.
        var baseStatus = parse(BaseStatus.class, request.baseStatus(), "baseStatus");
        var headStatus = parse(HeadStatus.class, request.headStatus(), "headStatus");
        var labelStatus = parse(LabelStatus.class, request.labelStatus(), "labelStatus");
        var assemblyStatus = parse(AssemblyStatus.class, request.assemblyStatus(), "assemblyStatus");
        var boxStatus = parse(BoxStatus.class, request.boxStatus(), "boxStatus");
        var shippingGuideStatus = parse(ShippingGuideStatus.class, request.shippingGuideStatus(), "shippingGuideStatus");
        var packagingStatus = parse(PackagingStatus.class, request.packagingStatus(), "packagingStatus");
        var carrierStatus = parse(CarrierStatus.class, request.carrierStatus(), "carrierStatus");
        // READ_COMMITTED and the parent lock ensure waiting requests load fresh candle/item states,
        // including on databases whose default isolation is REPEATABLE_READ.
        var orderId = repository.findOrderIdById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pet candle", id));
        var order = lockOrder(orderId);
        var candle = require(id);
        if (baseStatus != null) candle.setBaseStatus(baseStatus);
        if (headStatus != null) candle.setHeadStatus(headStatus);
        if (labelStatus != null) candle.setLabelStatus(labelStatus);
        if (assemblyStatus != null) candle.setAssemblyStatus(assemblyStatus);
        if (boxStatus != null) candle.setBoxStatus(boxStatus);
        if (shippingGuideStatus != null) candle.setShippingGuideStatus(shippingGuideStatus);
        if (packagingStatus != null) candle.setPackagingStatus(packagingStatus);
        if (carrierStatus != null) candle.setCarrierStatus(carrierStatus);
        repository.save(candle);
        if (isComplete(candle)) {
            var item = candle.getOrderItem();
            item.setStatus(OrderItemStatus.COMPLETED);
            items.save(item);
            // The shared persistence context includes the just-completed item in this collection.
            if (!order.getItems().isEmpty() && order.getItems().stream()
                    .allMatch(line -> line.getStatus() == OrderItemStatus.COMPLETED)) {
                order.setStatus(OrderStatus.COMPLETED);
                orders.save(order);
            }
        }
        return mapper.toResponse(candle);
    }

    private boolean isComplete(PetCandleEntity candle) {
        return candle.getBaseStatus() == BaseStatus.COMPLETED
                && candle.getHeadStatus() == HeadStatus.AT_WORKSHOP_ONE
                && candle.getLabelStatus() == LabelStatus.AT_WORKSHOP_ONE
                && candle.getAssemblyStatus() == AssemblyStatus.ASSEMBLED
                && candle.getBoxStatus() == BoxStatus.AVAILABLE
                && candle.getShippingGuideStatus() == ShippingGuideStatus.AT_WORKSHOP_ONE
                && candle.getPackagingStatus() == PackagingStatus.PACKAGED
                && candle.getCarrierStatus() == CarrierStatus.DELIVERED;
    }

    private <E extends Enum<E>> E parse(Class<E> type, String value, String field) {
        if (value == null) return null;
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException exception) {
            throw new InvalidPetCandleException("Invalid " + field + ": " + value);
        }
    }

    private PetCandleEntity require(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Pet candle", id));
    }

    private OrderEntity lockOrder(UUID id) {
        return orders.findByIdForUpdate(id).orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }
}
