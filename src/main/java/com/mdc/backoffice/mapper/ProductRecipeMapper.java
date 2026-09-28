package com.mdc.backoffice.mapper;

import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.model.entity.ProductRecipeEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProductRecipeMapper {
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "materialId", source = "material.id")
    ProductRecipeResponseDTO toResponse(ProductRecipeEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "material", ignore = true)
    ProductRecipeEntity toEntity(ProductRecipeRequestDTO request);

    @InheritConfiguration(name = "toEntity")
    void update(ProductRecipeRequestDTO request, @MappingTarget ProductRecipeEntity entity);
}

