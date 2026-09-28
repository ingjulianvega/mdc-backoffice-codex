package com.mdc.backoffice.repository;
import com.mdc.backoffice.model.entity.CustomerEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;
public interface CustomerRepository extends JpaRepository<CustomerEntity, UUID>, JpaSpecificationExecutor<CustomerEntity> {
    boolean existsByDocumentNumber(String value);
    boolean existsByDocumentNumberAndIdNot(String value, UUID id);
    boolean existsByEmailIgnoreCase(String value);
    boolean existsByEmailIgnoreCaseAndIdNot(String value, UUID id);
}
