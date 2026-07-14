package com.mybikelog.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class PageDTO<T> {
    private Integer page;
    private Integer size;
    private Integer totalElements;
    private Integer totalPages;
    private List<T> content;
}
