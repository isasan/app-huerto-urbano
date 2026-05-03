package com.huertoapp.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardResponse {
    private long totalGardens;
    private long totalCrops;
    private long pendingTasks;
    private long overdueTasks;
    private List<HarvestResponse> recentHarvests;
    private List<CropResponse> cropsReadyToHarvest;
    private List<TaskResponse> tasksDueToday;
}
