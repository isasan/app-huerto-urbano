package com.huertoapp.dto.request;

import com.huertoapp.model.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CalendarPlantRequest(
        @NotBlank String plantName,
        @NotBlank String emoji,
        @NotNull User.Hemisphere hemisphere,
        @NotBlank String difficulty,
        @Positive int daysToHarvest,
        List<Integer> sewingMonths,
        List<Integer> transplantMonths,
        List<Integer> harvestMonths,
        String latinName,
        String wateringFrequency,
        String pests,
        String wikipediaUrl
) {}
