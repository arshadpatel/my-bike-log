package com.mybikelog.api.dto;

import lombok.Data;

@Data
public class RidePageDTO {
    private Integer page;
    private Integer size;
    private Integer totalElements;
    private Integer totalPages;
    private RideDTO content;
}
