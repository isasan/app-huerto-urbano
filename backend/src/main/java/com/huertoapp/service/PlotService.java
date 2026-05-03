package com.huertoapp.service;

import com.huertoapp.dto.request.PlotRequest;
import com.huertoapp.dto.response.PlotResponse;
import com.huertoapp.exception.ResourceNotFoundException;
import com.huertoapp.model.Garden;
import com.huertoapp.model.Plot;
import com.huertoapp.model.User;
import com.huertoapp.repository.GardenRepository;
import com.huertoapp.repository.PlotRepository;
import com.huertoapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlotService {

    private final PlotRepository plotRepository;
    private final GardenRepository gardenRepository;
    private final UserRepository userRepository;

    public List<PlotResponse> getPlotsByGarden(Long gardenId, String username) {
        validateGardenOwnership(gardenId, username);
        return plotRepository.findByGardenId(gardenId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PlotResponse createPlot(Long gardenId, PlotRequest request, String username) {
        Garden garden = validateGardenOwnership(gardenId, username);
        Plot plot = Plot.builder()
                .name(request.getName())
                .sizeM2(request.getSizeM2())
                .soilType(request.getSoilType())
                .garden(garden)
                .build();
        return toResponse(plotRepository.save(plot));
    }

    @Transactional
    public PlotResponse updatePlot(Long gardenId, Long plotId, PlotRequest request, String username) {
        validateGardenOwnership(gardenId, username);
        Plot plot = plotRepository.findByIdAndGardenId(plotId, gardenId)
                .orElseThrow(() -> new ResourceNotFoundException("Parcela", plotId));
        plot.setName(request.getName());
        plot.setSizeM2(request.getSizeM2());
        plot.setSoilType(request.getSoilType());
        return toResponse(plotRepository.save(plot));
    }

    @Transactional
    public void deletePlot(Long gardenId, Long plotId, String username) {
        validateGardenOwnership(gardenId, username);
        Plot plot = plotRepository.findByIdAndGardenId(plotId, gardenId)
                .orElseThrow(() -> new ResourceNotFoundException("Parcela", plotId));
        plotRepository.delete(plot);
    }

    private Garden validateGardenOwnership(Long gardenId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
        return gardenRepository.findByIdAndUserId(gardenId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Huerto", gardenId));
    }

    private PlotResponse toResponse(Plot plot) {
        return PlotResponse.builder()
                .id(plot.getId())
                .name(plot.getName())
                .sizeM2(plot.getSizeM2())
                .soilType(plot.getSoilType())
                .gardenId(plot.getGarden().getId())
                .build();
    }
}
