package com.huertoapp.service;

import com.huertoapp.dto.request.CropRequest;
import com.huertoapp.dto.response.CropResponse;
import com.huertoapp.exception.ResourceNotFoundException;
import com.huertoapp.model.Crop;
import com.huertoapp.model.Plot;
import com.huertoapp.model.User;
import com.huertoapp.repository.CropRepository;
import com.huertoapp.repository.PlotRepository;
import com.huertoapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CropService {

    private final CropRepository cropRepository;
    private final PlotRepository plotRepository;
    private final UserRepository userRepository;

    public List<CropResponse> getUserCrops(String username) {
        User user = getUser(username);
        return cropRepository.findByPlotGardenUserId(user.getId())
                .stream().map(this::toResponse).toList();
    }

    public List<CropResponse> getCropsByPlot(Long plotId, String username) {
        validatePlotOwnership(plotId, username);
        return cropRepository.findByPlotId(plotId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public CropResponse createCrop(Long plotId, CropRequest request, String username) {
        Plot plot = validatePlotOwnership(plotId, username);
        Crop crop = Crop.builder()
                .plantName(request.getPlantName())
                .variety(request.getVariety())
                .plantedDate(request.getPlantedDate())
                .expectedHarvestDate(request.getExpectedHarvestDate())
                .status(Crop.Status.PLANTED)
                .notes(request.getNotes())
                .plot(plot)
                .build();
        return toResponse(cropRepository.save(crop));
    }

    @Transactional
    public CropResponse updateCrop(Long plotId, Long cropId, CropRequest request, String username) {
        validatePlotOwnership(plotId, username);
        Crop crop = cropRepository.findByIdAndPlotId(cropId, plotId)
                .orElseThrow(() -> new ResourceNotFoundException("Cultivo", cropId));
        crop.setPlantName(request.getPlantName());
        crop.setVariety(request.getVariety());
        crop.setPlantedDate(request.getPlantedDate());
        crop.setExpectedHarvestDate(request.getExpectedHarvestDate());
        crop.setNotes(request.getNotes());
        return toResponse(cropRepository.save(crop));
    }

    @Transactional
    public void deleteCrop(Long plotId, Long cropId, String username) {
        validatePlotOwnership(plotId, username);
        Crop crop = cropRepository.findByIdAndPlotId(cropId, plotId)
                .orElseThrow(() -> new ResourceNotFoundException("Cultivo", cropId));
        cropRepository.delete(crop);
    }

    private Plot validatePlotOwnership(Long plotId, String username) {
        User user = getUser(username);
        return plotRepository.findByIdAndGardenUserId(plotId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Parcela", plotId));
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }

    private CropResponse toResponse(Crop c) {
        return CropResponse.builder()
                .id(c.getId())
                .plantName(c.getPlantName())
                .variety(c.getVariety())
                .plantedDate(c.getPlantedDate())
                .expectedHarvestDate(c.getExpectedHarvestDate())
                .status(calculateStatus(c))
                .notes(c.getNotes())
                .plotId(c.getPlot().getId())
                .plotName(c.getPlot().getName())
                .build();
    }

    private Crop.Status calculateStatus(Crop crop) {
        LocalDate today = LocalDate.now();
        if (crop.getExpectedHarvestDate() == null) {
            return Crop.Status.PLANTED;
        }
        if (!crop.getExpectedHarvestDate().isAfter(today)) {
            return Crop.Status.READY;
        }
        if (crop.getPlantedDate() != null
                && ChronoUnit.DAYS.between(crop.getPlantedDate(), today) >= 30) {
            return Crop.Status.GROWING;
        }
        return Crop.Status.PLANTED;
    }
}
