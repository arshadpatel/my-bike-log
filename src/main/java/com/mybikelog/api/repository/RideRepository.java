package com.mybikelog.api.repository;

import com.mybikelog.api.entity.RideEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RideRepository extends JpaRepository<RideEntity, UUID> {

    Optional<RideEntity> findTopByBikeIdOrderByOdoDesc(UUID bikeId);

    Page<RideEntity> findByBikeIdOrderByCreatedAtDesc(UUID bikeId, Pageable pageable);

    Page<RideEntity> findByBikeIdAndDateBetweenOrderByCreatedAtDesc(UUID bikeId,
            LocalDate startDate, LocalDate endDate, Pageable pageable);

    Optional<RideEntity> findByIdAndBikeId(UUID rideId, UUID bikeId);

    @Query("SELECT DISTINCT r.date FROM RideEntity r WHERE r.bike.id = :bikeId")
    List<LocalDate> findDistinctDateByBikeId(UUID bikeId);
}
