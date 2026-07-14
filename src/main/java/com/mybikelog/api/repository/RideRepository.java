package com.mybikelog.api.repository;

import com.mybikelog.api.entity.RideEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RideRepository extends JpaRepository<RideEntity, UUID> {

    List<RideEntity> findByBikeIdOrderByOdoDesc(UUID bikeId);

    Optional<RideEntity> findTopByBikeIdOrderByOdoDesc(UUID bikeId);

    Page<RideEntity> findByBikeIdOrderByCreatedAtDesc(UUID bikeId, Pageable pageable);

    Optional<RideEntity> findByIdAndBikeId(UUID rideId, UUID bikeId);
}
