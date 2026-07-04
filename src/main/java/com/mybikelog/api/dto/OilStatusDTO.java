package com.mybikelog.api.dto;

import lombok.Data;

@Data
public class OilStatusDTO {
    private String status;
    private String lastChangeDate;
    private Double lastChangeOdo;
    private Double kmSinceLastChange;
    private Double kmRemaining;
    private Double percentUsed;
    private String message;
}
