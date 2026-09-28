package com.mdc.backoffice.model.dto;

import java.util.*;
import jakarta.validation.constraints.*;

public record PetCandlePhotoRequestDTO(@NotNull UUID petCandleId, @NotBlank @Size(max = 500) String photoUrl, Boolean isPrimary) {}
