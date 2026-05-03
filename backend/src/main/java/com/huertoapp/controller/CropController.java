package com.huertoapp.controller;

import com.huertoapp.dto.request.CropRequest;
import com.huertoapp.dto.response.CropResponse;
import com.huertoapp.service.CropService;
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
@RequiredArgsConstructor
@Tag(name = "Cultivos")
@SecurityRequirement(name = "bearerAuth")
public class CropController {

    private final CropService cropService;

    @GetMapping("/api/crops/user")
    @Operation(summary = "Obtener todos mis cultivos")
    public ResponseEntity<List<CropResponse>> getMyCrops(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(cropService.getUserCrops(user.getUsername()));
    }

    @GetMapping("/api/plots/{plotId}/crops")
    @Operation(summary = "Obtener cultivos de una parcela")
    public ResponseEntity<List<CropResponse>> getCropsByPlot(@PathVariable Long plotId,
                                                               @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(cropService.getCropsByPlot(plotId, user.getUsername()));
    }

    @PostMapping("/api/plots/{plotId}/crops")
    @Operation(summary = "Crear cultivo en una parcela")
    public ResponseEntity<CropResponse> createCrop(@PathVariable Long plotId,
                                                     @Valid @RequestBody CropRequest request,
                                                     @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cropService.createCrop(plotId, request, user.getUsername()));
    }

    @PutMapping("/api/plots/{plotId}/crops/{cropId}")
    @Operation(summary = "Actualizar cultivo")
    public ResponseEntity<CropResponse> updateCrop(@PathVariable Long plotId,
                                                     @PathVariable Long cropId,
                                                     @Valid @RequestBody CropRequest request,
                                                     @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(cropService.updateCrop(plotId, cropId, request, user.getUsername()));
    }

    @DeleteMapping("/api/plots/{plotId}/crops/{cropId}")
    @Operation(summary = "Eliminar cultivo")
    public ResponseEntity<Void> deleteCrop(@PathVariable Long plotId,
                                            @PathVariable Long cropId,
                                            @AuthenticationPrincipal UserDetails user) {
        cropService.deleteCrop(plotId, cropId, user.getUsername());
        return ResponseEntity.noContent().build();
    }
}
