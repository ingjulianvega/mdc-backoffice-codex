package com.mdc.backoffice.mapper;

import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.model.entity.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {CustomerMapper.class, OrderItemMapper.class},
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface OrderMapper {
    OrderResponseDTO toResponse(OrderEntity entity);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "orderDate", source = "orderDate")
    OrderEntity toEntity(OrderRequestDTO request);
}
