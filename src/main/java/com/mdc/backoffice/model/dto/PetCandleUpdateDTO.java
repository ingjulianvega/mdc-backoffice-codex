package com.mdc.backoffice.model.dto;

import java.util.*;
import jakarta.validation.constraints.*;

public record PetCandleUpdateDTO(@NotBlank @Size(max = 255) String petName, @Size(max = 100) String labelType,
        UUID aromaMaterialId, String customMessage, @Size(max = 100) String specie, @Size(max = 100) String breed) {}
