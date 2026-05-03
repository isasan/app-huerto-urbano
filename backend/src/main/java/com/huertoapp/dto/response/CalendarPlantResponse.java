package com.huertoapp.dto.response;

import java.util.List;

public record CalendarPlantResponse(
        String name,
        String emoji,
        String difficulty,
        int daysToHarvest,
        List<Integer> sewingMonths,
        List<Integer> transplantMonths,
        List<Integer> harvestMonths,
        boolean canSow,
        boolean canTransplant,
        boolean canHarvest,
        String latinName,
        String wateringFrequency,
        String pests,
        String wikipediaUrl
) {}
