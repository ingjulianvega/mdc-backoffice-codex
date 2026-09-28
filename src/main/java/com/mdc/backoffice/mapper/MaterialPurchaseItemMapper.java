package com.mdc.backoffice.mapper;

import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.model.entity.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MaterialPurchaseItemMapper {
    @Mapping(target = "materialId", source = "material.id")
    @Mapping(target = "materialName", source = "material.name")
    MaterialPurchaseItemResponseDTO toResponse(MaterialPurchaseItemEntity entity);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "unitCost", source = "unitCost")
    MaterialPurchaseItemEntity toEntity(MaterialPurchaseItemRequestDTO request);
}
