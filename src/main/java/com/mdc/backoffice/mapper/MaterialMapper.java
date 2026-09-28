package com.mdc.backoffice.mapper;

import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.model.entity.MaterialEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MaterialMapper {
    MaterialResponseDTO toResponse(MaterialEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    MaterialEntity toEntity(MaterialRequestDTO request);

    @InheritConfiguration(name = "toEntity")
    void update(MaterialRequestDTO request, @MappingTarget MaterialEntity entity);
}

