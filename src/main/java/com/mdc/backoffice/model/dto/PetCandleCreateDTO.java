package com.mdc.backoffice.model.dto;

import java.util.*;
import jakarta.validation.constraints.*;

public record PetCandleCreateDTO(@NotNull UUID orderItemId) {}
