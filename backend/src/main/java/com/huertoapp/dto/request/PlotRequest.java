package com.huertoapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PlotRequest {
    @NotBlank
    private String name;
    private Double sizeM2;
    private String soilType;
}
