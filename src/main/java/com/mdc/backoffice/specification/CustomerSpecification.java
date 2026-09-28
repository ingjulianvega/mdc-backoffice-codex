package com.mdc.backoffice.specification;
import com.mdc.backoffice.model.entity.CustomerEntity;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;
public final class CustomerSpecification {
    private CustomerSpecification() {}
    public static Specification<CustomerEntity> filter(String name, String email, String documentNumber) {
        return contains("firstName", name).or(contains("lastName", name)).and(contains("email", email).and(contains("documentNumber", documentNumber)));
    }
    private static Specification<CustomerEntity> contains(String field, String value) {
        return (root, query, builder) -> {
            if (value == null || value.isBlank()) return builder.conjunction();
            String escaped = value.toLowerCase(Locale.ROOT).replace("!", "!!").replace("%", "!%").replace("_", "!_");
            return builder.like(builder.lower(root.get(field)), "%" + escaped + "%", '!');
        };
    }
}
