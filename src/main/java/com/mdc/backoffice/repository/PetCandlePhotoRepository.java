package com.mdc.backoffice.repository;
import com.mdc.backoffice.model.entity.PetCandlePhotoEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PetCandlePhotoRepository extends JpaRepository<PetCandlePhotoEntity, UUID> {
    List<PetCandlePhotoEntity> findAllByPetCandleId(UUID petCandleId);
}
