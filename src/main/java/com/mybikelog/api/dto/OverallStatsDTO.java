package com.mybikelog.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class OverallStatsDTO {
    private Double totalKm;
    private Double totalLitres;
    private Double totalSpend;
    private Double overallAvgMileage;
    private Double overallMaxMileage;
    private List<MonthlyBreakdown> monthlyBreakdown;
}
