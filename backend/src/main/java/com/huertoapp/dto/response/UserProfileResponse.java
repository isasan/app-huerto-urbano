package com.huertoapp.dto.response;

import com.huertoapp.model.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String role;
    private String city;
    private String countryCode;
    private User.Hemisphere hemisphere;
    private LocalDateTime createdAt;
}
