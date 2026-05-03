package com.huertoapp.dto.request;

import com.huertoapp.model.HarvestLog;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class HarvestRequest {
    @NotNull
    private LocalDate harvestDate;
    private Double quantity;
    private String unit;
    private HarvestLog.Quality quality;
    private String notes;
    @NotNull
    private Long cropId;
}
