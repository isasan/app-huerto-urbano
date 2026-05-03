package com.huertoapp.service;

import com.huertoapp.dto.request.HarvestRequest;
import com.huertoapp.dto.response.HarvestResponse;
import com.huertoapp.exception.ResourceNotFoundException;
import com.huertoapp.model.Crop;
import com.huertoapp.model.HarvestLog;
import com.huertoapp.model.User;
import com.huertoapp.repository.CropRepository;
import com.huertoapp.repository.HarvestRepository;
import com.huertoapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HarvestService {

    private final HarvestRepository harvestRepository;
    private final CropRepository cropRepository;
    private final UserRepository userRepository;

    public List<HarvestResponse> getUserHarvests(String username) {
        User user = getUser(username);
        return harvestRepository.findByCropPlotGardenUserId(user.getId())
                .stream()
                .sorted((a, b) -> b.getHarvestDate().compareTo(a.getHarvestDate()))
                .map(this::toResponse)
                .toList();
    }

    public List<HarvestResponse> getHarvestsByCrop(Long cropId, String username) {
        User user = getUser(username);
        return harvestRepository.findByCropIdAndCropPlotGardenUserId(cropId, user.getId())
                .stream()
                .sorted((a, b) -> b.getHarvestDate().compareTo(a.getHarvestDate()))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public HarvestResponse createHarvest(HarvestRequest request, String username) {
        User user = getUser(username);
        Crop crop = cropRepository.findByPlotGardenUserId(user.getId())
                .stream()
                .filter(c -> c.getId().equals(request.getCropId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cultivo", request.getCropId()));

        HarvestLog harvest = HarvestLog.builder()
                .harvestDate(request.getHarvestDate())
                .quantity(request.getQuantity())
                .unit(request.getUnit())
                .quality(request.getQuality())
                .notes(request.getNotes())
                .crop(crop)
                .build();

        return toResponse(harvestRepository.save(harvest));
    }

    @Transactional
    public HarvestResponse updateHarvest(Long id, HarvestRequest request, String username) {
        User user = getUser(username);
        HarvestLog harvest = harvestRepository.findByCropPlotGardenUserId(user.getId())
                .stream()
                .filter(h -> h.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cosecha", id));

        harvest.setHarvestDate(request.getHarvestDate());
        harvest.setQuantity(request.getQuantity());
        harvest.setUnit(request.getUnit());
        harvest.setQuality(request.getQuality());
        harvest.setNotes(request.getNotes());

        return toResponse(harvestRepository.save(harvest));
    }

    @Transactional
    public void deleteHarvest(Long id, String username) {
        User user = getUser(username);
        HarvestLog harvest = harvestRepository.findByCropPlotGardenUserId(user.getId())
                .stream()
                .filter(h -> h.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cosecha", id));
        harvestRepository.delete(harvest);
    }

    HarvestResponse toResponse(HarvestLog h) {
        return HarvestResponse.builder()
                .id(h.getId())
                .harvestDate(h.getHarvestDate())
                .quantity(h.getQuantity())
                .unit(h.getUnit())
                .quality(h.getQuality())
                .notes(h.getNotes())
                .cropId(h.getCrop().getId())
                .plantName(h.getCrop().getPlantName())
                .gardenId(h.getCrop().getPlot().getGarden().getId())
                .gardenName(h.getCrop().getPlot().getGarden().getName())
                .build();
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }
}
