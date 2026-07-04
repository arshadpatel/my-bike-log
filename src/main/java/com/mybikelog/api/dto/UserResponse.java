package com.mybikelog.api.dto;

import lombok.Data;

@Data
public class UserResponse {
    private String id;
    private String email;
    private String name;
    private String pictureUrl;
    private String activeBikeId;
    private String createdAt;
}
