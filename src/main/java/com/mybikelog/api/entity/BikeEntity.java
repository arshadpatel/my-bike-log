package com.mybikelog.api.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
public class BikeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String name;
    private Double initialOdo;
    private Double currentOdo;
    private Double oilChangeIntervalKm;
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity user;
}
