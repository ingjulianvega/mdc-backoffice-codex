package com.mdc.backoffice.mapper;

import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.model.entity.SupplierEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface SupplierMapper {
    SupplierResponseDTO toResponse(SupplierEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    SupplierEntity toEntity(SupplierRequestDTO request);

    @InheritConfiguration(name = "toEntity")
    void update(SupplierRequestDTO request, @MappingTarget SupplierEntity entity);
}
