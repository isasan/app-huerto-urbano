package com.huertoapp.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PlantDetailRequest(
        @NotBlank String plantName,
        String latinName,
        String wateringFrequency,
        String pests,
        String wikipediaUrl
) {}
