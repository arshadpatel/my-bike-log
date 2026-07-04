package com.mybikelog.api.dto;

import lombok.Data;

@Data
public class TyreSideStatus {
    private String lastCheckedDate;
    private Double psi;
    private String notes;
}
