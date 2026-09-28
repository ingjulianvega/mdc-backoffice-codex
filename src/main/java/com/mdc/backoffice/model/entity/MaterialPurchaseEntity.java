package com.mdc.backoffice.model.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "MATERIAL_PURCHASES")
public class MaterialPurchaseEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private SupplierEntity supplier;
    @Column(name = "purchase_date", nullable = false)
    private Instant purchaseDate;
    @Column(name = "total_cost", nullable = false, precision = 24, scale = 8)
    private BigDecimal totalCost;
    @OneToMany(mappedBy = "materialPurchase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MaterialPurchaseItemEntity> items = new ArrayList<>();
}
