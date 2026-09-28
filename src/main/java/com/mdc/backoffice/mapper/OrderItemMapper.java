package com.mdc.backoffice.mapper;

import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.model.entity.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface OrderItemMapper {
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "addressId", source = "address.id")
    @Mapping(target = "status", source = "status")
    OrderItemResponseDTO toResponse(OrderItemEntity entity);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "unitPrice", source = "unitPrice")
    @Mapping(target = "trackingNumber", source = "trackingNumber")
    @Mapping(target = "carrierName", source = "carrierName")
    OrderItemEntity toEntity(OrderItemRequestDTO request);
}
