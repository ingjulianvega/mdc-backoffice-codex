package com.mdc.backoffice.repository;
import com.mdc.backoffice.model.entity.SupplierEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;
public interface SupplierRepository extends JpaRepository<SupplierEntity, UUID>, JpaSpecificationExecutor<SupplierEntity> {
    boolean existsByEmailIgnoreCase(String value);
    boolean existsByEmailIgnoreCaseAndIdNot(String value, UUID id);
    boolean existsByTaxId(String value);
    boolean existsByTaxIdAndIdNot(String value, UUID id);
}
