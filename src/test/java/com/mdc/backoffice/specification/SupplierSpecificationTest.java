package com.mdc.backoffice.specification;

import com.mdc.backoffice.model.entity.SupplierEntity;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

class SupplierSpecificationTest {
    @SuppressWarnings("unchecked")
    @Test
    void filtersEscapeWildcardsAndIgnoreCase() {
        Root<SupplierEntity> root = mock(Root.class);
        CriteriaBuilder builder = mock(CriteriaBuilder.class);
        Path<String> path = mock(Path.class);
        Expression<String> lower = mock(Expression.class);
        when(root.<String>get(anyString())).thenReturn(path);
        when(builder.lower(path)).thenReturn(lower);
        SupplierSpecification.filter("TEST%_!", "TEST%_!").toPredicate(root, null, builder);
        verify(root).get("companyName");
        verify(root).get("taxId");
        verify(builder, times(2)).like(lower, "%test!%!_!!%", '!');
    }

    @SuppressWarnings("unchecked")
    @Test
    void emptyFiltersDoNotAccessFields() {
        Root<SupplierEntity> root = mock(Root.class);
        CriteriaBuilder builder = mock(CriteriaBuilder.class);
        SupplierSpecification.filter(null, "").toPredicate(root, null, builder);
        verifyNoInteractions(root);
    }
}
