package com.mybikelog.api.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class MonthlyBreakdown {
    private String month;
    private Double km;
    private Double litres;
    private Double spend;
}
