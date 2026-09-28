package com.mdc.backoffice.model.dto;

import java.util.*;
import jakarta.validation.constraints.*;

public record PetCandlePhotoResponseDTO(UUID id, UUID petCandleId, String photoUrl, Boolean isPrimary) {}
