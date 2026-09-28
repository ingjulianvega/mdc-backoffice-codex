package com.mdc.backoffice.specification;

import com.mdc.backoffice.model.entity.CustomerEntity;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

class CustomerSpecificationTest {
    @SuppressWarnings("unchecked")
    @Test
    void filtersEscapeWildcardsAndIgnoreCase() {
        Root<CustomerEntity> root = mock(Root.class);
        CriteriaBuilder builder = mock(CriteriaBuilder.class);
        Path<String> path = mock(Path.class);
        Expression<String> lower = mock(Expression.class);
        when(root.<String>get(anyString())).thenReturn(path);
        when(builder.lower(path)).thenReturn(lower);
        CustomerSpecification.filter("TEST%_!", "TEST%_!", "TEST%_!").toPredicate(root, null, builder);
        verify(root).get("firstName");
        verify(root).get("lastName");
        verify(root).get("email");
        verify(root).get("documentNumber");
        verify(builder, times(4)).like(lower, "%test!%!_!!%", '!');
    }

    @SuppressWarnings("unchecked")
    @Test
    void emptyFiltersDoNotAccessFields() {
        Root<CustomerEntity> root = mock(Root.class);
        CriteriaBuilder builder = mock(CriteriaBuilder.class);
        CustomerSpecification.filter(null, "", null).toPredicate(root, null, builder);
        verifyNoInteractions(root);
    }
}
