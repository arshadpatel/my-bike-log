package com.mybikelog.api.dto;

import lombok.Data;

@Data
public class PageDTO {
    private Integer page;
    private Integer size;
    private Integer totalElements;
    private Integer totalPages;
    private RideAndPetrolDTO content;
}
