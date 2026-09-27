package com.mdc.backoffice.specification;

import com.mdc.backoffice.model.entity.DepartmentEntity;
import org.springframework.data.jpa.domain.Specification;

public final class DepartmentSpecification {
    private DepartmentSpecification() {}
    public static Specification<DepartmentEntity> all() {
        return (root, query, builder) -> builder.conjunction();
    }
}
