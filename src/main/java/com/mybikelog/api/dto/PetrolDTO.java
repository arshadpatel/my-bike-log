package com.mybikelog.api.dto;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class PetrolDTO {
    private UUID id;
    private UUID bikeId;
    private String date;
    private Double odo;
    private Double amount;
    private Double pricePerLitre;
    private Double litres;
    private Double cumulativeLitres;
    private Double mileageKmPerLitre;
    private Double distanceKm;
    private Instant createdAt;
}
