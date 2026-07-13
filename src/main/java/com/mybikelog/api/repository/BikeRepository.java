package com.mybikelog.api.repository;

import com.mybikelog.api.entity.BikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BikeRepository extends JpaRepository<BikeEntity, UUID> {
    List<BikeEntity> findByUserId(UUID userId);

    Optional<BikeEntity> findByIdAndUserId(UUID bikeId, UUID userId);
}
