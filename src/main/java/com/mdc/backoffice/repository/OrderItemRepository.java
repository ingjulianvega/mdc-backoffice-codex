package com.mdc.backoffice.repository;

import com.mdc.backoffice.model.entity.OrderItemEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, UUID>,
        JpaSpecificationExecutor<OrderItemEntity> {}
