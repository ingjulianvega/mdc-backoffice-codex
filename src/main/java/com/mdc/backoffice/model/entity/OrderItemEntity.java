package com.mdc.backoffice.model.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ORDER_ITEMS")
public class OrderItemEntity extends AuditableEntity {
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private com.mdc.backoffice.model.enum_.OrderItemStatus status =
            com.mdc.backoffice.model.enum_.OrderItemStatus.PENDING;
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    @Column(name = "unit_price", nullable = false)
    private Long unitPrice;
    @Column(name = "subtotal", nullable = false)
    private Long subtotal;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = true)
    private AddressEntity address;
    @Column(name = "tracking_number", length = 100, nullable = true)
    private String trackingNumber;
    @Column(name = "carrier_name", length = 100, nullable = true)
    private String carrierName;
}
