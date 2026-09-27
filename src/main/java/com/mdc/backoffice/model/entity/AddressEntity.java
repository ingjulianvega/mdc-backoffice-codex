package com.mdc.backoffice.model.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ADDRESSES")
public class AddressEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "street_address", nullable = false, length = 255)
    private String streetAddress;
    @Column(name = "additional_info", length = 255)
    private String additionalInfo;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "city_id", nullable = false)
    private CityEntity city;
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
}