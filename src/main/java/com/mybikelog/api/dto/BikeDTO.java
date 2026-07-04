package com.mybikelog.api.dto;

import lombok.Data;

@Data
public class BikeDTO {
    private String id;
    private String name;
    private double initialOdo;
    private double currentOdo;
    private double oilChangeIntervalKm;
    private String createdAt;
}
