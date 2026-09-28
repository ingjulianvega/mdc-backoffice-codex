package com.mdc.backoffice.specification;

import com.mdc.backoffice.model.entity.OrderEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class OrderSpecification {
    private OrderSpecification() {}

    public static Specification<OrderEntity> hasCustomer(UUID customerId) {
        return (root, query, builder) -> customerId == null ? builder.conjunction()
                : builder.equal(root.get("customer").get("id"), customerId);
    }

    public static Specification<OrderEntity> orderedFrom(Instant startDate) {
        return (root, query, builder) -> startDate == null ? builder.conjunction()
                : builder.greaterThanOrEqualTo(root.get("orderDate"), startDate);
    }

    public static Specification<OrderEntity> orderedUntil(Instant endDate) {
        return (root, query, builder) -> endDate == null ? builder.conjunction()
                : builder.lessThanOrEqualTo(root.get("orderDate"), endDate);
    }
}
