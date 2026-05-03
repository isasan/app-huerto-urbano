package com.huertoapp.controller;

import com.huertoapp.dto.response.PlantDetailResponse;
import com.huertoapp.service.PlantDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plant-details")
@RequiredArgsConstructor
@Tag(name = "Fichas de plantas")
public class PlantDetailController {

    private final PlantDetailService plantDetailService;

    @GetMapping
    @Operation(summary = "Listar todas las fichas de plantas")
    public ResponseEntity<List<PlantDetailResponse>> getAll() {
        return ResponseEntity.ok(plantDetailService.findAll());
    }

    @GetMapping("/{plantName}")
    @Operation(summary = "Obtener ficha de una planta por nombre")
    public ResponseEntity<PlantDetailResponse> getByName(@PathVariable String plantName) {
        return ResponseEntity.ok(plantDetailService.findByPlantName(plantName));
    }
}
