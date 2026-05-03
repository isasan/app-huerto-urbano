package com.huertoapp.controller;

import com.huertoapp.dto.request.GardenRequest;
import com.huertoapp.dto.response.GardenResponse;
import com.huertoapp.service.GardenService;
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
@RequestMapping("/api/gardens")
@RequiredArgsConstructor
@Tag(name = "Huertos")
@SecurityRequirement(name = "bearerAuth")
public class GardenController {

    private final GardenService gardenService;

    @GetMapping
    @Operation(summary = "Obtener mis huertos")
    public ResponseEntity<List<GardenResponse>> getMyGardens(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(gardenService.getUserGardens(user.getUsername()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener huerto por ID")
    public ResponseEntity<GardenResponse> getGarden(@PathVariable Long id,
                                                     @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(gardenService.getGardenById(id, user.getUsername()));
    }

    @PostMapping
    @Operation(summary = "Crear huerto")
    public ResponseEntity<GardenResponse> createGarden(@Valid @RequestBody GardenRequest request,
                                                        @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(gardenService.createGarden(request, user.getUsername()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar huerto")
    public ResponseEntity<GardenResponse> updateGarden(@PathVariable Long id,
                                                        @Valid @RequestBody GardenRequest request,
                                                        @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(gardenService.updateGarden(id, request, user.getUsername()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar huerto")
    public ResponseEntity<Void> deleteGarden(@PathVariable Long id,
                                              @AuthenticationPrincipal UserDetails user) {
        gardenService.deleteGarden(id, user.getUsername());
        return ResponseEntity.noContent().build();
    }
}
