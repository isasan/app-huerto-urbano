package com.huertoapp.dto.response;

import com.huertoapp.model.Crop;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class CropResponse {
    private Long id;
    private String plantName;
    private String variety;
    private LocalDate plantedDate;
    private LocalDate expectedHarvestDate;
    private Crop.Status status;
    private String notes;
    private Long plotId;
    private String plotName;
}
