package com.mybikelog.api.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetrolEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private LocalDate date;
    private Double odo;
    private Double amount;
    private Double pricePerLitre;
    private Double litres;
    private Double cumulativeLitres;
    private Double distanceKm;
    private Double mileageKmPerLitre;
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private BikeEntity bike;
}