package com.mdc.backoffice.repository;

import com.mdc.backoffice.model.entity.MaterialEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MaterialRepository extends JpaRepository<MaterialEntity, UUID>, JpaSpecificationExecutor<MaterialEntity> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select m from MaterialEntity m where m.id = :id")
    java.util.Optional<MaterialEntity> findByIdForUpdate(@org.springframework.data.repository.query.Param("id") UUID id);
}
