package com.mdc.backoffice.repository;

import com.mdc.backoffice.model.entity.ProductRecipeEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRecipeRepository extends JpaRepository<ProductRecipeEntity, UUID>, JpaSpecificationExecutor<ProductRecipeEntity> {
}

