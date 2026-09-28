package com.mdc.backoffice.specification;

import com.mdc.backoffice.model.entity.ProductEntity;
import jakarta.persistence.criteria.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductSpecificationTest {
    @Mock Root<ProductEntity> root;
    @Mock CriteriaQuery<?> query;
    @Mock CriteriaBuilder builder;
    @Mock Predicate predicate;
    @Mock Path<String> name;
    @Mock Expression<String> lowerName;

    @Test
    void nameSearchIsCaseInsensitiveAndEscapesWildcards() {
        when(root.<String>get("name")).thenReturn(name);
        when(builder.lower(name)).thenReturn(lowerName);
        when(builder.like(lowerName, "%wax!%!_!!%", '!')).thenReturn(predicate);
        assertSame(predicate, ProductSpecification.hasName("WAX%_!").toPredicate(root, query, builder));
    }

    @Test
    void absentOrBlankNameDoesNotRestrictResults() {
        when(builder.conjunction()).thenReturn(predicate);
        for (String value : new String[] {null, "", " "}) {
            assertSame(predicate, ProductSpecification.hasName(value).toPredicate(root, query, builder));
        }
        verifyNoInteractions(root);
    }
}

