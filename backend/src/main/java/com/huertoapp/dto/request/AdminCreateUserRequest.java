package com.huertoapp.dto.request;

import com.huertoapp.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminCreateUserRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @Email @NotBlank String email,
        @NotBlank @Size(min = 6) String password,
        User.Role role,
        String city,
        String countryCode,
        User.Hemisphere hemisphere
) {}
