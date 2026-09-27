package com.mdc.backoffice.repository;

import com.mdc.backoffice.model.entity.DepartmentEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DepartmentRepository extends JpaRepository<DepartmentEntity, UUID>,
        JpaSpecificationExecutor<DepartmentEntity> {
}
