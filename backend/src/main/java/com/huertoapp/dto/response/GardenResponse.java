package com.huertoapp.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class GardenResponse {
    private Long id;
    private String name;
    private String description;
    private String location;
    private Double sizeM2;
    private Long userId;
    private int plotCount;
    private LocalDateTime createdAt;
}
