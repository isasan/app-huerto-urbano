package com.huertoapp.dto.request;

import com.huertoapp.model.User;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @Size(max = 100, message = "La ciudad no puede tener más de 100 caracteres")
    private String city;

    @Pattern(regexp = "^[A-Z]{2}$", message = "El código de país debe ser de 2 letras mayúsculas (ISO 3166-1 alpha-2)")
    private String countryCode;

    private User.Hemisphere hemisphere;

    @Size(max = 10, message = "Avatar inválido")
    private String avatar;
}
