package com.mybikelog.api.dto;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class UsersDTO {
    private UUID id;
    private String email;
    private String name;
    private String pictureUrl;
    private UUID activeBikeId;
    private Instant createdAt;
}
