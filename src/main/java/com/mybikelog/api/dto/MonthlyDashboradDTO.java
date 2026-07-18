package com.mybikelog.api.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class MonthlyDashboradDTO {
    private String month;
    private Double kmDrivenThisMonth;
    private Double litresThisMonth;
    private Double spendThisMonth;
    private Double currentMileage;
    private Double bestMileageThisMonth;
    private Integer ridingDaysThisMonth;
    private Integer totalRidesThisMonth;
    private Double totalOdo;
    private OilStatusDTO oilStatus;
}
