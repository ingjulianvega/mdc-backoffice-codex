package com.mdc.backoffice.repository;

import com.mdc.backoffice.model.entity.MaterialEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MaterialRepository extends JpaRepository<MaterialEntity, UUID>, JpaSpecificationExecutor<MaterialEntity> {
}

