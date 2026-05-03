package com.huertoapp.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminUserResponse {
    private Long id;
    private String username;
    private String email;
    private String role;
    private String city;
    private String countryCode;
    private String hemisphere;
    private LocalDateTime createdAt;
}
