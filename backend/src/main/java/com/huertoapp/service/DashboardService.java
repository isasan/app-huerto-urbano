package com.huertoapp.service;

import com.huertoapp.dto.response.CropResponse;
import com.huertoapp.dto.response.DashboardResponse;
import com.huertoapp.dto.response.HarvestResponse;
import com.huertoapp.dto.response.TaskResponse;
import com.huertoapp.exception.ResourceNotFoundException;
import com.huertoapp.model.Crop;
import com.huertoapp.model.Task;
import com.huertoapp.model.User;
import com.huertoapp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final GardenRepository gardenRepository;
    private final CropRepository cropRepository;
    private final TaskRepository taskRepository;
    private final HarvestRepository harvestRepository;
    private final UserRepository userRepository;
    private final HarvestService harvestService;

    public DashboardResponse getDashboard(String username) {
        User user = getUser(username);
        LocalDate today = LocalDate.now();

        long totalGardens = gardenRepository.countByUserId(user.getId());

        List<Crop> allCrops = cropRepository.findByPlotGardenUserId(user.getId());
        long totalCrops = allCrops.stream()
                .filter(c -> computeStatus(c) != Crop.Status.HARVESTED)
                .count();
        List<CropResponse> cropsReadyToHarvest = allCrops.stream()
                .filter(c -> computeStatus(c) == Crop.Status.READY)
                .map(this::toCropResponse)
                .toList();

        long pendingTasks = taskRepository.countByGardenUserIdAndCompleted(user.getId(), false);
        long overdueTasks = taskRepository.countByGardenUserIdAndCompletedAndDueDateBefore(user.getId(), false, today);

        List<TaskResponse> tasksDueToday = taskRepository
                .findByGardenUserIdAndDueDateAndCompleted(user.getId(), today, false)
                .stream()
                .map(this::toTaskResponse)
                .toList();

        List<HarvestResponse> recentHarvests = harvestRepository
                .findTop5ByCropPlotGardenUserIdOrderByHarvestDateDesc(user.getId())
                .stream()
                .map(harvestService::toResponse)
                .toList();

        return DashboardResponse.builder()
                .totalGardens(totalGardens)
                .totalCrops(totalCrops)
                .pendingTasks(pendingTasks)
                .overdueTasks(overdueTasks)
                .recentHarvests(recentHarvests)
                .cropsReadyToHarvest(cropsReadyToHarvest)
                .tasksDueToday(tasksDueToday)
                .build();
    }

    private Crop.Status computeStatus(Crop crop) {
        if (crop.getStatus() == Crop.Status.HARVESTED) return Crop.Status.HARVESTED;
        LocalDate today = LocalDate.now();
        if (crop.getExpectedHarvestDate() != null && !crop.getExpectedHarvestDate().isAfter(today)) {
            return Crop.Status.READY;
        }
        if (crop.getPlantedDate() != null
                && ChronoUnit.DAYS.between(crop.getPlantedDate(), today) >= 30) {
            return Crop.Status.GROWING;
        }
        return Crop.Status.PLANTED;
    }

    private CropResponse toCropResponse(Crop c) {
        return CropResponse.builder()
                .id(c.getId())
                .plantName(c.getPlantName())
                .variety(c.getVariety())
                .plantedDate(c.getPlantedDate())
                .expectedHarvestDate(c.getExpectedHarvestDate())
                .status(computeStatus(c))
                .notes(c.getNotes())
                .plotId(c.getPlot().getId())
                .plotName(c.getPlot().getName())
                .build();
    }

    private TaskResponse toTaskResponse(Task t) {
        return TaskResponse.builder()
                .id(t.getId())
                .title(t.getTitle())
                .type(t.getType())
                .dueDate(t.getDueDate())
                .completed(t.getCompleted())
                .notes(t.getNotes())
                .gardenId(t.getGarden().getId())
                .gardenName(t.getGarden().getName())
                .cropId(t.getCrop() != null ? t.getCrop().getId() : null)
                .cropName(t.getCrop() != null ? t.getCrop().getPlantName() : null)
                .build();
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }
}
