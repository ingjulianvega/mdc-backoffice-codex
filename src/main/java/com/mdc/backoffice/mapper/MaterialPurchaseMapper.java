package com.mdc.backoffice.mapper;

import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.model.entity.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {SupplierMapper.class, MaterialPurchaseItemMapper.class},
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MaterialPurchaseMapper {
    MaterialPurchaseResponseDTO toResponse(MaterialPurchaseEntity entity);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "purchaseDate", source = "purchaseDate")
    MaterialPurchaseEntity toEntity(MaterialPurchaseRequestDTO request);
}
