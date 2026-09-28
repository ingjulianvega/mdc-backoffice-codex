package com.mdc.backoffice.specification;

import com.mdc.backoffice.model.entity.MaterialEntity;
import java.util.UUID;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

public final class MaterialSpecification {
    private MaterialSpecification() {}
    public static Specification<MaterialEntity> hasName(String name) {
        return (root, query, builder) -> {
            if (name == null || name.isBlank()) return builder.conjunction();
            String escaped = name.toLowerCase(Locale.ROOT).replace("!", "!!").replace("%", "!%").replace("_", "!_");
            return builder.like(builder.lower(root.get("name")), "%" + escaped + "%", '!');
        };
    }
}

