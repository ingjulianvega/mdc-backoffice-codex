package com.mdc.backoffice.model.entity;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "SUPPLIERS")
public class SupplierEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "company_name", length = 150, nullable = false)
    private String companyName;
    @Column(name = "contact_name", length = 100, nullable = true)
    private String contactName;
    @Column(name = "email", length = 150, nullable = true, unique = true)
    private String email;
    @Column(name = "phone", length = 30, nullable = true)
    private String phone;
    @Column(name = "tax_id", length = 30, nullable = false, unique = true)
    private String taxId;
}
