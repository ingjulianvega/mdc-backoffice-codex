package com.mdc.backoffice.mapper;

import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.model.entity.AddressEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = CityMapper.class, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AddressMapper {
    AddressResponseDTO toResponse(AddressEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "city", ignore = true)
    AddressEntity toEntity(AddressRequestDTO request);

    @InheritConfiguration(name = "toEntity")
    void update(AddressRequestDTO request, @MappingTarget AddressEntity entity);
}
