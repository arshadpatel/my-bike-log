package com.mybikelog.api.repository;

import com.mybikelog.api.entity.PetrolEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PetrolRepository extends JpaRepository<PetrolEntity, UUID> {
    Page<PetrolEntity> findByBikeIdOrderByCreatedAtDesc(UUID bikeId,
                                                        Pageable pageable);

    Page<PetrolEntity> findByBikeIdAndDateBetweenOrderByCreatedAtDesc(UUID bikeId,
                LocalDate startDate, LocalDate endDate, Pageable pageable);

    Optional<PetrolEntity> findByIdAndBikeId(UUID petrolEntryId,
                                             UUID bikeId);

    Optional<PetrolEntity> findTopByBikeIdOrderByOdoDesc(UUID bikeId);

    List<PetrolEntity> findTop2ByBikeIdOrderByOdoDesc(UUID bikeId);
}
