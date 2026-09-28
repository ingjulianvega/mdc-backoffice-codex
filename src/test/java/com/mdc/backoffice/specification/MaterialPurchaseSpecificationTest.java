package com.mdc.backoffice.specification;

import com.mdc.backoffice.model.entity.MaterialPurchaseEntity;
import jakarta.persistence.criteria.*;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaterialPurchaseSpecificationTest {
    @Mock Root<MaterialPurchaseEntity> root;
    @Mock CriteriaQuery<?> query;
    @Mock CriteriaBuilder builder;
    @Mock Predicate predicate;
    @Mock Path<Object> supplier;
    @Mock Path<Object> supplierIdPath;
    @Mock Path<Instant> datePath;

    @Test
    void filtersSupplierByUuid() {
        var id = UUID.randomUUID();
        when(root.get("supplier")).thenReturn(supplier);
        when(supplier.get("id")).thenReturn(supplierIdPath);
        when(builder.equal(supplierIdPath, id)).thenReturn(predicate);
        assertSame(predicate, MaterialPurchaseSpecification.hasSupplier(id).toPredicate(root, query, builder));
    }

    @Test
    void dateBoundsAreInclusive() {
        var date = Instant.parse("2026-09-27T12:00:00Z");
        when(root.<Instant>get("purchaseDate")).thenReturn(datePath);
        when(builder.greaterThanOrEqualTo(datePath, date)).thenReturn(predicate);
        when(builder.lessThanOrEqualTo(datePath, date)).thenReturn(predicate);
        assertSame(predicate, MaterialPurchaseSpecification.purchasedFrom(date).toPredicate(root, query, builder));
        assertSame(predicate, MaterialPurchaseSpecification.purchasedUntil(date).toPredicate(root, query, builder));
    }

    @Test
    void absentFiltersDoNotRestrictResults() {
        when(builder.conjunction()).thenReturn(predicate);
        assertSame(predicate, MaterialPurchaseSpecification.hasSupplier(null).toPredicate(root, query, builder));
        assertSame(predicate, MaterialPurchaseSpecification.purchasedFrom(null).toPredicate(root, query, builder));
        assertSame(predicate, MaterialPurchaseSpecification.purchasedUntil(null).toPredicate(root, query, builder));
        verifyNoInteractions(root);
    }
}
