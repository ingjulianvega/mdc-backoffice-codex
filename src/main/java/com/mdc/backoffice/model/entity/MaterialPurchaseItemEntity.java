package com.mdc.backoffice.model.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "MATERIAL_PURCHASE_ITEMS")
public class MaterialPurchaseItemEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_purchase_id", nullable = false)
    private MaterialPurchaseEntity materialPurchase;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_id", nullable = false)
    private MaterialEntity material;
    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal quantity;
    @Column(name = "unit_cost", nullable = false, precision = 12, scale = 4)
    private BigDecimal unitCost;
    @Column(nullable = false, precision = 24, scale = 8)
    private BigDecimal subtotal;
}
