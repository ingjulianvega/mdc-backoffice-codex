package com.mdc.backoffice.specification;

import com.mdc.backoffice.model.entity.ProductRecipeEntity;
import jakarta.persistence.criteria.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductRecipeSpecificationTest {
    @Mock Root<ProductRecipeEntity> root;
    @Mock CriteriaQuery<?> query;
    @Mock CriteriaBuilder builder;
    @Mock Predicate predicate;
    @Mock Path<Object> product;
    @Mock Path<Object> material;
    @Mock Path<Object> productIdPath;
    @Mock Path<Object> materialIdPath;
    @Mock Predicate materialPredicate;
    @Mock Predicate combined;

    @Test
    void filtersUseRelatedUuidsAndCombineWithAnd() {
        var productId = UUID.randomUUID();
        var materialId = UUID.randomUUID();
        when(root.get("product")).thenReturn(product);
        when(root.get("material")).thenReturn(material);
        when(product.get("id")).thenReturn(productIdPath);
        when(material.get("id")).thenReturn(materialIdPath);
        when(builder.equal(productIdPath, productId)).thenReturn(predicate);
        when(builder.equal(materialIdPath, materialId)).thenReturn(materialPredicate);
        when(builder.and(predicate, materialPredicate)).thenReturn(combined);
        assertSame(combined, ProductRecipeSpecification.hasProductId(productId)
                .and(ProductRecipeSpecification.hasMaterialId(materialId)).toPredicate(root, query, builder));
    }

    @Test
    void absentFiltersDoNotRestrictResults() {
        when(builder.conjunction()).thenReturn(predicate);
        assertSame(predicate, ProductRecipeSpecification.hasProductId(null).toPredicate(root, query, builder));
        assertSame(predicate, ProductRecipeSpecification.hasMaterialId(null).toPredicate(root, query, builder));
        verifyNoInteractions(root);
    }
}

