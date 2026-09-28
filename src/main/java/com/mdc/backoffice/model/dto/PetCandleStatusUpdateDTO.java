package com.mdc.backoffice.model.dto;

import java.util.*;
import jakarta.validation.constraints.*;

public record PetCandleStatusUpdateDTO(String baseStatus, String headStatus, String labelStatus, String assemblyStatus, String boxStatus, String shippingGuideStatus, String packagingStatus, String carrierStatus) {}
