package com.huertoapp.dto.response;

import java.util.List;

public record CalendarPlantAdminResponse(
        Long id,
        String plantName,
        String emoji,
        String hemisphere,
        String difficulty,
        int daysToHarvest,
        List<Integer> sewingMonths,
        List<Integer> transplantMonths,
        List<Integer> harvestMonths,
        String latinName,
        String wateringFrequency,
        String pests,
        String wikipediaUrl
) {}
