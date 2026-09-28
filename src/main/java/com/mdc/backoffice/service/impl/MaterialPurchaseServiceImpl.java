package com.mdc.backoffice.service.impl;

import com.mdc.backoffice.exception.*;
import com.mdc.backoffice.mapper.*;
import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.model.entity.*;
import com.mdc.backoffice.repository.*;
import com.mdc.backoffice.service.MaterialPurchaseService;
import com.mdc.backoffice.specification.MaterialPurchaseSpecification;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MaterialPurchaseServiceImpl implements MaterialPurchaseService {
    private static final BigDecimal MAX_STOCK = new BigDecimal("99999999.9999");
    private static final BigDecimal MAX_TOTAL = new BigDecimal("9999999999999999.99999999");
    private final MaterialPurchaseRepository repository;
    private final SupplierRepository supplierRepository;
    private final MaterialRepository materialRepository;
    private final MaterialPurchaseMapper mapper;
    private final MaterialPurchaseItemMapper itemMapper;

    @Override
    @Transactional
    public MaterialPurchaseResponseDTO create(MaterialPurchaseRequestDTO request) {
        var supplier = supplierRepository.findById(request.supplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", request.supplierId()));
        var purchase = mapper.toEntity(request);
        purchase.setSupplier(supplier);
        purchase.setPurchaseDate(request.purchaseDate() == null ? Instant.now() : request.purchaseDate());
        // Lock each distinct material in a stable order to serialize inventory changes.
        var materials = lockMaterials(request.items().stream().map(MaterialPurchaseItemRequestDTO::materialId).toList());
        var quantities = new LinkedHashMap<UUID, BigDecimal>();
        BigDecimal total = BigDecimal.ZERO;
        for (var requestItem : request.items()) {
            var item = itemMapper.toEntity(requestItem);
            item.setMaterialPurchase(purchase);
            item.setMaterial(materials.get(requestItem.materialId()));
            item.setSubtotal(item.getQuantity().multiply(item.getUnitCost()));
            total = total.add(item.getSubtotal());
            quantities.merge(requestItem.materialId(), item.getQuantity(), BigDecimal::add);
            purchase.getItems().add(item);
        }
        if (total.compareTo(MAX_TOTAL) > 0) {
            throw new InvalidMaterialPurchaseException("Purchase total exceeds the supported amount.");
        }
        purchase.setTotalCost(total);
        updateStock(materials, quantities, false);
        return mapper.toResponse(repository.save(purchase));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MaterialPurchaseResponseDTO> findAll(UUID supplierId, Instant startDate, Instant endDate, Pageable pageable) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new InvalidMaterialPurchaseException("startDate must not be after endDate.");
        }
        var specification = MaterialPurchaseSpecification.hasSupplier(supplierId)
                .and(MaterialPurchaseSpecification.purchasedFrom(startDate))
                .and(MaterialPurchaseSpecification.purchasedUntil(endDate));
        return repository.findAll(specification, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public MaterialPurchaseResponseDTO findById(UUID id) {
        return mapper.toResponse(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material purchase", id)));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        var purchase = repository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material purchase", id));
        var quantities = new LinkedHashMap<UUID, BigDecimal>();
        purchase.getItems().forEach(item ->
                quantities.merge(item.getMaterial().getId(), item.getQuantity(), BigDecimal::add));
        var materials = lockMaterials(quantities.keySet());
        updateStock(materials, quantities, true);
        repository.delete(purchase);
    }

    private Map<UUID, MaterialEntity> lockMaterials(Collection<UUID> ids) {
        var materials = new LinkedHashMap<UUID, MaterialEntity>();
        ids.stream().distinct().sorted().forEach(id -> materials.put(id,
                materialRepository.findByIdForUpdate(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Material", id))));
        return materials;
    }

    private void updateStock(Map<UUID, MaterialEntity> materials, Map<UUID, BigDecimal> quantities, boolean reverse) {
        var stocks = new LinkedHashMap<UUID, BigDecimal>();
        quantities.forEach((id, quantity) -> {
            var stock = materials.get(id).getCurrentStock().add(reverse ? quantity.negate() : quantity);
            if (stock.signum() < 0 || stock.compareTo(MAX_STOCK) > 0) {
                throw new InvalidMaterialPurchaseException("Stock out of range for material: " + id);
            }
            stocks.put(id, stock);
        });
        stocks.forEach((id, stock) -> {
            var material = materials.get(id);
            material.setCurrentStock(stock);
            materialRepository.save(material);
        });
    }
}
