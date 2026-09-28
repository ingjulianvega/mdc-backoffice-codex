package com.mdc.backoffice.model.dto;

import java.util.*;
import jakarta.validation.constraints.*;

public record PetCandleResponseDTO(UUID id, UUID orderItemId, String petName, String labelType, UUID aromaMaterialId,
        String customMessage, String specie, String breed,
        String baseStatus, String headStatus, String labelStatus, String assemblyStatus, String boxStatus, String shippingGuideStatus, String packagingStatus, String carrierStatus,
        List<PetCandlePhotoResponseDTO> photos) {}
