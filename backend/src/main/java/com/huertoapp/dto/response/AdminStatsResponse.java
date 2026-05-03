package com.huertoapp.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminStatsResponse {
    private long totalUsers;
    private long totalGardens;
    private long totalCrops;
    private long totalHarvests;
    private long totalTasks;
}
