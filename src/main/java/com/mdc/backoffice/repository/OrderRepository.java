package com.mdc.backoffice.repository;

import com.mdc.backoffice.model.entity.OrderEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID>,
        JpaSpecificationExecutor<OrderEntity> {
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from OrderEntity p where p.id = :id")
    java.util.Optional<OrderEntity> findByIdForUpdate(
            @org.springframework.data.repository.query.Param("id") UUID id);
}
