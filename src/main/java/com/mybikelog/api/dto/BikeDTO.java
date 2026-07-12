package com.mybikelog.api.dto;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class BikeDTO {
    private UUID id;
    private String name;
    private Double initialOdo;
    private Double currentOdo;
    private Double oilChangeIntervalKm;
    private Instant createdAt;
}
