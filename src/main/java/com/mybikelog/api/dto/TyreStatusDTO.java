package com.mybikelog.api.dto;

import lombok.Data;

@Data
public class TyreStatusDTO {
    private TyreSideStatus front;
    private TyreSideStatus rear;
}
