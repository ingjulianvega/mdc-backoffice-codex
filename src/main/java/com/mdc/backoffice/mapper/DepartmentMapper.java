package com.mdc.backoffice.mapper;

import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.model.entity.DepartmentEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DepartmentMapper {

    DepartmentResponseDTO toResponse(DepartmentEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)

    DepartmentEntity toEntity(DepartmentRequestDTO request);

    @InheritConfiguration(name = "toEntity")
    void update(DepartmentRequestDTO request, @MappingTarget DepartmentEntity entity);
}
