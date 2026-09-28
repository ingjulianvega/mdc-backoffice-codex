package com.mdc.backoffice.specification;
import com.mdc.backoffice.model.entity.SupplierEntity;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;
public final class SupplierSpecification {
    private SupplierSpecification() {}
    public static Specification<SupplierEntity> filter(String companyName, String taxId) {
        return contains("companyName", companyName).and(contains("taxId", taxId));
    }
    private static Specification<SupplierEntity> contains(String field, String value) {
        return (root, query, builder) -> {
            if (value == null || value.isBlank()) return builder.conjunction();
            String escaped = value.toLowerCase(Locale.ROOT).replace("!", "!!").replace("%", "!%").replace("_", "!_");
            return builder.like(builder.lower(root.get(field)), "%" + escaped + "%", '!');
        };
    }
}
