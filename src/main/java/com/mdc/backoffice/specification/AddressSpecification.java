package com.mdc.backoffice.specification;

import com.mdc.backoffice.model.entity.AddressEntity;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class AddressSpecification {
    private AddressSpecification() {}

    public static Specification<AddressEntity> hasCityId(UUID cityId) {
        return (root, query, builder) -> cityId == null
                ? builder.conjunction()
                : builder.equal(root.get("city").get("id"), cityId);
    }

    public static Specification<AddressEntity> hasCustomerId(UUID customerId) {
        return (root, query, builder) -> customerId == null
                ? builder.conjunction()
                : builder.equal(root.get("customerId"), customerId);
    }
}