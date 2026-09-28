package com.mdc.backoffice.repository;

import com.mdc.backoffice.model.entity.ProductEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRepository extends JpaRepository<ProductEntity, UUID>, JpaSpecificationExecutor<ProductEntity> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select p from ProductEntity p where p.id = :id")
    java.util.Optional<ProductEntity> findByIdForUpdate(
            @org.springframework.data.repository.query.Param("id") UUID id);
}

