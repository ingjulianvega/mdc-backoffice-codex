package com.mdc.backoffice.model.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "PET_CANDLE_PHOTOS")
public class PetCandlePhotoEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_candle_id", nullable = false)
    private PetCandleEntity petCandle;
    @Column(name = "photo_url", length = 500, nullable = false)
    private String photoUrl;
    @Column(name = "is_primary", nullable = true)
    private Boolean isPrimary;
}
