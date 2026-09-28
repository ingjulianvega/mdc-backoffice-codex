package com.mdc.backoffice.specification;

import com.mdc.backoffice.model.entity.ProductRecipeEntity;
import java.util.UUID;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

public final class ProductRecipeSpecification {
    private ProductRecipeSpecification() {}
    public static Specification<ProductRecipeEntity> hasProductId(UUID id) {
        return (root, query, builder) -> id == null ? builder.conjunction()
                : builder.equal(root.get("product").get("id"), id);
    }
    public static Specification<ProductRecipeEntity> hasMaterialId(UUID id) {
        return (root, query, builder) -> id == null ? builder.conjunction()
                : builder.equal(root.get("material").get("id"), id);
    }
}

