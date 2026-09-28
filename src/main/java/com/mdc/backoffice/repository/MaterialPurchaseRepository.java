package com.mdc.backoffice.repository;

import com.mdc.backoffice.model.entity.MaterialPurchaseEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;

public interface MaterialPurchaseRepository extends JpaRepository<MaterialPurchaseEntity, UUID>,
        JpaSpecificationExecutor<MaterialPurchaseEntity> {
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from MaterialPurchaseEntity p where p.id = :id")
    java.util.Optional<MaterialPurchaseEntity> findByIdForUpdate(
            @org.springframework.data.repository.query.Param("id") UUID id);
}
