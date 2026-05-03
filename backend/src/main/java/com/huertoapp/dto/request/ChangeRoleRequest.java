package com.huertoapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ChangeRoleRequest {
    @NotBlank(message = "El rol es obligatorio")
    @Pattern(regexp = "USER|ADMIN", message = "El rol debe ser USER o ADMIN")
    private String role;
}
