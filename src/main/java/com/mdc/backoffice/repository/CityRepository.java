package com.mdc.backoffice.repository;

import com.mdc.backoffice.model.entity.CityEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CityRepository extends JpaRepository<CityEntity, UUID>,
        JpaSpecificationExecutor<CityEntity> {
}
