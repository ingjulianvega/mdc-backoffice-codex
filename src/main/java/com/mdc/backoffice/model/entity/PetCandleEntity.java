package com.mdc.backoffice.model.entity;

import jakarta.persistence.*;
import java.util.*;
import lombok.*;
import com.mdc.backoffice.model.enum_.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "PET_CANDLE")
public class PetCandleEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItemEntity orderItem;
    @Column(name = "pet_name", length = 255, nullable = false)
    private String petName;
    @Column(name = "label_type", length = 100, nullable = true)
    private String labelType;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aroma_material_id", nullable = true)
    private MaterialEntity aromaMaterial;
    @Column(name = "custom_message", columnDefinition = "TEXT", nullable = true)
    private String customMessage;
    @Column(name = "specie", length = 100, nullable = true)
    private String specie;
    @Column(name = "breed", length = 100, nullable = true)
    private String breed;
    @Enumerated(EnumType.STRING)
    @Column(name = "base_status", length = 50, nullable = true)
    private BaseStatus baseStatus = BaseStatus.PENDING;
    @Enumerated(EnumType.STRING)
    @Column(name = "head_status", length = 50, nullable = true)
    private HeadStatus headStatus = HeadStatus.PENDING;
    @Enumerated(EnumType.STRING)
    @Column(name = "label_status", length = 50, nullable = true)
    private LabelStatus labelStatus = LabelStatus.PENDING;
    @Enumerated(EnumType.STRING)
    @Column(name = "assembly_status", length = 50, nullable = true)
    private AssemblyStatus assemblyStatus = AssemblyStatus.PENDING;
    @Enumerated(EnumType.STRING)
    @Column(name = "box_status", length = 50, nullable = true)
    private BoxStatus boxStatus = BoxStatus.PENDING;
    @Enumerated(EnumType.STRING)
    @Column(name = "shipping_guide_status", length = 50, nullable = true)
    private ShippingGuideStatus shippingGuideStatus = ShippingGuideStatus.PENDING;
    @Enumerated(EnumType.STRING)
    @Column(name = "packaging_status", length = 50, nullable = true)
    private PackagingStatus packagingStatus = PackagingStatus.PENDING;
    @Enumerated(EnumType.STRING)
    @Column(name = "carrier_status", length = 50, nullable = true)
    private CarrierStatus carrierStatus = CarrierStatus.PENDING;
    @OneToMany(mappedBy = "petCandle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PetCandlePhotoEntity> photos = new ArrayList<>();
}
