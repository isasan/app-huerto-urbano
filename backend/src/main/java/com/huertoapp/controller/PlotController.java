package com.huertoapp.controller;

import com.huertoapp.dto.request.PlotRequest;
import com.huertoapp.dto.response.PlotResponse;
import com.huertoapp.service.PlotService;
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
@RequestMapping("/api/gardens/{gardenId}/plots")
@RequiredArgsConstructor
@Tag(name = "Parcelas")
@SecurityRequirement(name = "bearerAuth")
public class PlotController {

    private final PlotService plotService;

    @GetMapping
    @Operation(summary = "Obtener parcelas de un huerto")
    public ResponseEntity<List<PlotResponse>> getPlots(@PathVariable Long gardenId,
                                                        @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(plotService.getPlotsByGarden(gardenId, user.getUsername()));
    }

    @PostMapping
    @Operation(summary = "Crear parcela en un huerto")
    public ResponseEntity<PlotResponse> createPlot(@PathVariable Long gardenId,
                                                    @Valid @RequestBody PlotRequest request,
                                                    @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(plotService.createPlot(gardenId, request, user.getUsername()));
    }

    @PutMapping("/{plotId}")
    @Operation(summary = "Actualizar parcela")
    public ResponseEntity<PlotResponse> updatePlot(@PathVariable Long gardenId,
                                                    @PathVariable Long plotId,
                                                    @Valid @RequestBody PlotRequest request,
                                                    @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(plotService.updatePlot(gardenId, plotId, request, user.getUsername()));
    }

    @DeleteMapping("/{plotId}")
    @Operation(summary = "Eliminar parcela")
    public ResponseEntity<Void> deletePlot(@PathVariable Long gardenId,
                                            @PathVariable Long plotId,
                                            @AuthenticationPrincipal UserDetails user) {
        plotService.deletePlot(gardenId, plotId, user.getUsername());
        return ResponseEntity.noContent().build();
    }
}
