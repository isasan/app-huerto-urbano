package com.huertoapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CropRequest {
    @NotBlank
    private String plantName;
    private String variety;
    @NotNull
    private LocalDate plantedDate;
    private LocalDate expectedHarvestDate;
    private String notes;
}
