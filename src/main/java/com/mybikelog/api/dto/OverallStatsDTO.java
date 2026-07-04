package com.mybikelog.api.dto;

import lombok.Data;

@Data
public class OverallStatsDTO {
    private Double totalKm;
    private Double totalLitres;
    private Double totalSpend;
    private Double overallAvgMileage;
    private Double overallMaxMileage;
    private MonthlyBreakdown monthlyBreakdown;
}
