package com.mdc.backoffice.mapper;
import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.model.entity.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PetCandlePhotoMapper {
    @Mapping(target = "petCandleId", source = "petCandle.id")
    PetCandlePhotoResponseDTO toResponse(PetCandlePhotoEntity entity);
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "photoUrl", source = "photoUrl")
    @Mapping(target = "isPrimary", source = "isPrimary")
    PetCandlePhotoEntity toEntity(PetCandlePhotoRequestDTO request);
}
