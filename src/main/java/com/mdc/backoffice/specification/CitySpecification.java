package com.mdc.backoffice.specification;

import com.mdc.backoffice.model.entity.CityEntity;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class CitySpecification {
    private CitySpecification() {}
    public static Specification<CityEntity> hasDepartmentId(UUID departmentId) {
        return (root, query, builder) -> departmentId == null
                ? builder.conjunction()
                : builder.equal(root.get("department").get("id"), departmentId);
    }
}
