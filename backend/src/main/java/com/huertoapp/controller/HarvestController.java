package com.huertoapp.controller;

import com.huertoapp.dto.request.HarvestRequest;
import com.huertoapp.dto.response.HarvestResponse;
import com.huertoapp.service.HarvestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/harvests")
@RequiredArgsConstructor
@Tag(name = "Cosechas")
@SecurityRequirement(name = "bearerAuth")
public class HarvestController {

    private final HarvestService harvestService;

    @GetMapping
    @Operation(summary = "Obtener mis cosechas (filtro opcional: ?cropId=X)")
    public ResponseEntity<List<HarvestResponse>> getMyHarvests(
            @RequestParam(required = false) Long cropId,
            @AuthenticationPrincipal UserDetails user) {
        if (cropId != null) {
            return ResponseEntity.ok(harvestService.getHarvestsByCrop(cropId, user.getUsername()));
        }
        return ResponseEntity.ok(harvestService.getUserHarvests(user.getUsername()));
    }

    @PostMapping
    @Operation(summary = "Registrar cosecha")
    public ResponseEntity<HarvestResponse> createHarvest(
            @Valid @RequestBody HarvestRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(harvestService.createHarvest(request, user.getUsername()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cosecha")
    public ResponseEntity<HarvestResponse> updateHarvest(
            @PathVariable Long id,
            @Valid @RequestBody HarvestRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(harvestService.updateHarvest(id, request, user.getUsername()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar cosecha")
    public ResponseEntity<Void> deleteHarvest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user) {
        harvestService.deleteHarvest(id, user.getUsername());
        return ResponseEntity.noContent().build();
    }
}
