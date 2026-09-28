package com.mdc.backoffice.specification;

import com.mdc.backoffice.model.entity.MaterialPurchaseEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class MaterialPurchaseSpecification {
    private MaterialPurchaseSpecification() {}

    public static Specification<MaterialPurchaseEntity> hasSupplier(UUID supplierId) {
        return (root, query, builder) -> supplierId == null ? builder.conjunction()
                : builder.equal(root.get("supplier").get("id"), supplierId);
    }

    public static Specification<MaterialPurchaseEntity> purchasedFrom(Instant startDate) {
        return (root, query, builder) -> startDate == null ? builder.conjunction()
                : builder.greaterThanOrEqualTo(root.get("purchaseDate"), startDate);
    }

    public static Specification<MaterialPurchaseEntity> purchasedUntil(Instant endDate) {
        return (root, query, builder) -> endDate == null ? builder.conjunction()
                : builder.lessThanOrEqualTo(root.get("purchaseDate"), endDate);
    }
}
