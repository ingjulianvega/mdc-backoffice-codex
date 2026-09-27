package com.mdc.backoffice.specification;

import com.mdc.backoffice.model.entity.AddressEntity;
import jakarta.persistence.criteria.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressSpecificationTest {
    @Mock Root<AddressEntity> root;
    @Mock CriteriaQuery<?> query;
    @Mock CriteriaBuilder builder;
    @Mock Path<Object> city;
    @Mock Path<Object> cityIdPath;
    @Mock Path<Object> customerIdPath;
    @Mock Predicate cityPredicate;
    @Mock Predicate customerPredicate;
    @Mock Predicate combined;

    @Test
    void cityFilterUsesRelatedCityId() {
        var id = UUID.randomUUID();
        when(root.get("city")).thenReturn(city);
        when(city.get("id")).thenReturn(cityIdPath);
        when(builder.equal(cityIdPath, id)).thenReturn(cityPredicate);
        assertSame(cityPredicate, AddressSpecification.hasCityId(id).toPredicate(root, query, builder));
    }

    @Test
    void customerFilterUsesScalarAttribute() {
        var id = UUID.randomUUID();
        when(root.get("customerId")).thenReturn(customerIdPath);
        when(builder.equal(customerIdPath, id)).thenReturn(customerPredicate);
        assertSame(customerPredicate, AddressSpecification.hasCustomerId(id).toPredicate(root, query, builder));
    }

    @Test
    void absentFiltersDoNotRestrictResults() {
        when(builder.conjunction()).thenReturn(combined);
        assertSame(combined, AddressSpecification.hasCityId(null).toPredicate(root, query, builder));
        assertSame(combined, AddressSpecification.hasCustomerId(null).toPredicate(root, query, builder));
        verifyNoInteractions(root);
    }

    @Test
    void filtersAreCombinedWithAnd() {
        var cityId = UUID.randomUUID();
        var customerId = UUID.randomUUID();
        when(root.get("city")).thenReturn(city);
        when(city.get("id")).thenReturn(cityIdPath);
        when(root.get("customerId")).thenReturn(customerIdPath);
        when(builder.equal(cityIdPath, cityId)).thenReturn(cityPredicate);
        when(builder.equal(customerIdPath, customerId)).thenReturn(customerPredicate);
        when(builder.and(cityPredicate, customerPredicate)).thenReturn(combined);
        assertSame(combined, AddressSpecification.hasCityId(cityId)
                .and(AddressSpecification.hasCustomerId(customerId)).toPredicate(root, query, builder));
    }
}
