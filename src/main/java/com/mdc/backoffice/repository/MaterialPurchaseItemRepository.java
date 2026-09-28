package com.mdc.backoffice.repository;

import com.mdc.backoffice.model.entity.MaterialPurchaseItemEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;

public interface MaterialPurchaseItemRepository extends JpaRepository<MaterialPurchaseItemEntity, UUID>,
        JpaSpecificationExecutor<MaterialPurchaseItemEntity> {}
