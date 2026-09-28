package com.mdc.backoffice.mapper;
import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.model.entity.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = PetCandlePhotoMapper.class, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PetCandleMapper {
    @Mapping(target = "orderItemId", source = "orderItem.id")
    @Mapping(target = "aromaMaterialId", source = "aromaMaterial.id")
    PetCandleResponseDTO toResponse(PetCandleEntity entity);
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "petName", source = "petName")
    @Mapping(target = "labelType", source = "labelType")
    @Mapping(target = "customMessage", source = "customMessage")
    @Mapping(target = "specie", source = "specie")
    @Mapping(target = "breed", source = "breed")
    void update(PetCandleUpdateDTO request, @MappingTarget PetCandleEntity entity);
}
