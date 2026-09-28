package com.mdc.backoffice.mapper;

import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.model.entity.CustomerEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CustomerMapper {
    CustomerResponseDTO toResponse(CustomerEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    CustomerEntity toEntity(CustomerRequestDTO request);

    @InheritConfiguration(name = "toEntity")
    void update(CustomerRequestDTO request, @MappingTarget CustomerEntity entity);
}
