package com.huertoapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeleteAccountRequest {
    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String username;
}
