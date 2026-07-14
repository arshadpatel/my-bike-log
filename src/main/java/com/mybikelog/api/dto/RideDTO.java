package com.mybikelog.api.dto;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class RideDTO {
    private UUID id;
    private UUID bikeId;
    private String date;
    private String time;
    private Double odo;
    private Double distanceKm;
    private Instant createdAt;
}
