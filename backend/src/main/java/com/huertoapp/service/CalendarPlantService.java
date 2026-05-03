package com.huertoapp.service;

import com.huertoapp.dto.request.CalendarPlantRequest;
import com.huertoapp.dto.response.CalendarPlantAdminResponse;
import com.huertoapp.exception.ResourceNotFoundException;
import com.huertoapp.model.CalendarPlant;
import com.huertoapp.repository.CalendarPlantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalendarPlantService {

    private final CalendarPlantRepository calendarPlantRepository;

    public List<CalendarPlantAdminResponse> findAll() {
        return calendarPlantRepository.findAllByOrderByPlantNameAsc()
                .stream().map(this::toAdminResponse).toList();
    }

    @Transactional
    public CalendarPlantAdminResponse create(CalendarPlantRequest req) {
        if (calendarPlantRepository.existsByPlantName(req.plantName())) {
            throw new IllegalArgumentException("Ya existe una planta con el nombre: " + req.plantName());
        }
        CalendarPlant plant = CalendarPlant.builder()
                .plantName(req.plantName())
                .emoji(req.emoji())
                .hemisphere(req.hemisphere())
                .difficulty(req.difficulty())
                .daysToHarvest(req.daysToHarvest())
                .sewingMonths(formatMonths(req.sewingMonths()))
                .transplantMonths(formatMonths(req.transplantMonths()))
                .harvestMonths(formatMonths(req.harvestMonths()))
                .latinName(req.latinName())
                .wateringFrequency(req.wateringFrequency())
                .pests(req.pests())
                .wikipediaUrl(req.wikipediaUrl())
                .build();
        return toAdminResponse(calendarPlantRepository.save(plant));
    }

    @Transactional
    public CalendarPlantAdminResponse update(Long id, CalendarPlantRequest req) {
        CalendarPlant plant = calendarPlantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CalendarPlant", id));
        plant.setPlantName(req.plantName());
        plant.setEmoji(req.emoji());
        plant.setHemisphere(req.hemisphere());
        plant.setDifficulty(req.difficulty());
        plant.setDaysToHarvest(req.daysToHarvest());
        plant.setSewingMonths(formatMonths(req.sewingMonths()));
        plant.setTransplantMonths(formatMonths(req.transplantMonths()));
        plant.setHarvestMonths(formatMonths(req.harvestMonths()));
        plant.setLatinName(req.latinName());
        plant.setWateringFrequency(req.wateringFrequency());
        plant.setPests(req.pests());
        plant.setWikipediaUrl(req.wikipediaUrl());
        return toAdminResponse(calendarPlantRepository.save(plant));
    }

    @Transactional
    public void delete(Long id) {
        if (!calendarPlantRepository.existsById(id)) {
            throw new ResourceNotFoundException("CalendarPlant", id);
        }
        calendarPlantRepository.deleteById(id);
    }

    public static List<Integer> parseMonths(String months) {
        if (months == null || months.isBlank()) return List.of();
        return Arrays.stream(months.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    private String formatMonths(List<Integer> months) {
        if (months == null || months.isEmpty()) return "";
        return months.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private CalendarPlantAdminResponse toAdminResponse(CalendarPlant p) {
        return new CalendarPlantAdminResponse(
                p.getId(),
                p.getPlantName(),
                p.getEmoji(),
                p.getHemisphere().name(),
                p.getDifficulty(),
                p.getDaysToHarvest(),
                parseMonths(p.getSewingMonths()),
                parseMonths(p.getTransplantMonths()),
                parseMonths(p.getHarvestMonths()),
                p.getLatinName(),
                p.getWateringFrequency(),
                p.getPests(),
                p.getWikipediaUrl()
        );
    }
}
