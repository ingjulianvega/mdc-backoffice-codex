package com.mdc.backoffice.repository;

import com.mdc.backoffice.model.entity.AddressEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AddressRepository extends JpaRepository<AddressEntity, UUID>,
        JpaSpecificationExecutor<AddressEntity> {
}
