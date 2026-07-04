package com.mybikelog.api.dto;

import lombok.Data;

@Data
public class BikeDTO {
    private String id;
    private String name;
    private Double initialOdo;
    private Double currentOdo;
    private Double oilChangeIntervalKm;
    private String createdAt;
}
