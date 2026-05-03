package com.huertoapp.dto.response;

public record PlantDetailResponse(
        Long id,
        String plantName,
        String latinName,
        String wateringFrequency,
        String pests,
        String wikipediaUrl
) {}
