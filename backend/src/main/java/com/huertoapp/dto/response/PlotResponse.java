package com.huertoapp.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlotResponse {
    private Long id;
    private String name;
    private Double sizeM2;
    private String soilType;
    private Long gardenId;
}
