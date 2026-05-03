package com.huertoapp.service;

import com.huertoapp.dto.request.GardenRequest;
import com.huertoapp.dto.response.GardenResponse;
import com.huertoapp.exception.ResourceNotFoundException;
import com.huertoapp.exception.UnauthorizedException;
import com.huertoapp.model.Garden;
import com.huertoapp.model.User;
import com.huertoapp.repository.GardenRepository;
import com.huertoapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GardenService {

    private final GardenRepository gardenRepository;
    private final UserRepository userRepository;

    public List<GardenResponse> getUserGardens(String username) {
        User user = getUser(username);
        return gardenRepository.findByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public GardenResponse getGardenById(Long id, String username) {
        User user = getUser(username);
        Garden garden = gardenRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Huerto", id));
        return toResponse(garden);
    }

    @Transactional
    public GardenResponse createGarden(GardenRequest request, String username) {
        User user = getUser(username);
        Garden garden = Garden.builder()
                .name(request.getName())
                .description(request.getDescription())
                .location(request.getLocation())
                .sizeM2(request.getSizeM2())
                .user(user)
                .build();
        return toResponse(gardenRepository.save(garden));
    }

    @Transactional
    public GardenResponse updateGarden(Long id, GardenRequest request, String username) {
        User user = getUser(username);
        Garden garden = gardenRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Huerto", id));
        garden.setName(request.getName());
        garden.setDescription(request.getDescription());
        garden.setLocation(request.getLocation());
        garden.setSizeM2(request.getSizeM2());
        return toResponse(gardenRepository.save(garden));
    }

    @Transactional
    public void deleteGarden(Long id, String username) {
        User user = getUser(username);
        Garden garden = gardenRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Huerto", id));
        gardenRepository.delete(garden);
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }

    private GardenResponse toResponse(Garden g) {
        return GardenResponse.builder()
                .id(g.getId())
                .name(g.getName())
                .description(g.getDescription())
                .location(g.getLocation())
                .sizeM2(g.getSizeM2())
                .userId(g.getUser().getId())
                .plotCount(g.getPlots() != null ? g.getPlots().size() : 0)
                .createdAt(g.getCreatedAt())
                .build();
    }
}
