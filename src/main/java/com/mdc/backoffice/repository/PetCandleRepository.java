package com.mdc.backoffice.repository;
import com.mdc.backoffice.model.entity.PetCandleEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PetCandleRepository extends JpaRepository<PetCandleEntity, UUID> {
    @org.springframework.data.jpa.repository.Query(
            "select c.orderItem.order.id from PetCandleEntity c where c.id = :id")
    Optional<UUID> findOrderIdById(@org.springframework.data.repository.query.Param("id") UUID id);
    Optional<PetCandleEntity> findByOrderItemId(UUID orderItemId);
}
