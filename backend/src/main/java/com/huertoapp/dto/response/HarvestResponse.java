package com.huertoapp.dto.response;

import com.huertoapp.model.HarvestLog;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class HarvestResponse {
    private Long id;
    private LocalDate harvestDate;
    private Double quantity;
    private String unit;
    private HarvestLog.Quality quality;
    private String notes;
    private Long cropId;
    private String plantName;
    private Long gardenId;
    private String gardenName;
}
