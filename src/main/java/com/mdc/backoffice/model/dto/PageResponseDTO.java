package com.mdc.backoffice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.data.domain.Page;

@Schema(description = "Pagina estable del contrato REST; la primera pagina tiene numero 0.")
public record PageResponseDTO<T>(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<T> content,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0") int number,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, minimum = "1") int size,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0") long totalElements,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0") int totalPages,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean first,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean last) {
    public static <T> PageResponseDTO<T> from(Page<T> page) {
        return new PageResponseDTO<>(List.copyOf(page.getContent()), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }
}
