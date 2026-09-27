package com.mdc.backoffice.mapper;

import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.model.entity.CityEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CityMapper {
    @Mapping(target = "departmentId", source = "department.id")
    CityResponseDTO toResponse(CityEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "department", ignore = true)
    CityEntity toEntity(CityRequestDTO request);

    @InheritConfiguration(name = "toEntity")
    void update(CityRequestDTO request, @MappingTarget CityEntity entity);
}
