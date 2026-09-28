package com.mdc.backoffice.model.entity;

import jakarta.persistence.*;
import java.util.UUID;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "MATERIALS")
public class MaterialEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, length = 150)
    private String name;
    @Column(name = "unit_of_measure", nullable = false, length = 50)
    private String unitOfMeasure;
    @Column(name = "current_stock", nullable = false, precision = 12, scale = 4)
    private BigDecimal currentStock;
}

