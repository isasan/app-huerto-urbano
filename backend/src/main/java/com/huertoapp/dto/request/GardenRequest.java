package com.huertoapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GardenRequest {
    @NotBlank
    private String name;
    private String description;
    private String location;
    private Double sizeM2;
}
