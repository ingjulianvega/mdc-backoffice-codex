package com.mdc.backoffice.model.entity;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "CUSTOMERS")
public class CustomerEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "document_type", length = 50, nullable = false)
    private String documentType;
    @Column(name = "document_number", length = 50, nullable = false, unique = true)
    private String documentNumber;
    @Column(name = "first_name", length = 100, nullable = false)
    private String firstName;
    @Column(name = "last_name", length = 100, nullable = false)
    private String lastName;
    @Column(name = "phone_number", length = 30, nullable = true)
    private String phoneNumber;
    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;
}
