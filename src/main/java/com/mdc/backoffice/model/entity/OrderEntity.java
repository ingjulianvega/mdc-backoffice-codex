package com.mdc.backoffice.model.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;
import lombok.*;
import com.mdc.backoffice.model.enum_.OrderStatus;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ORDERS")
public class OrderEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;
    @Column(name = "order_date", nullable = false)
    private Instant orderDate;
    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private OrderStatus status;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> items = new ArrayList<>();
}