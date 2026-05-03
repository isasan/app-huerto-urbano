package com.huertoapp.controller;

import com.huertoapp.dto.request.AdminCreateUserRequest;
import com.huertoapp.dto.request.CalendarPlantRequest;
import com.huertoapp.dto.request.ChangeRoleRequest;
import com.huertoapp.dto.request.PlantDetailRequest;
import com.huertoapp.dto.response.AdminStatsResponse;
import com.huertoapp.dto.response.AdminUserResponse;
import com.huertoapp.dto.response.CalendarPlantAdminResponse;
import com.huertoapp.dto.response.PlantDetailResponse;
import com.huertoapp.service.AdminService;
import com.huertoapp.service.CalendarPlantService;
import com.huertoapp.service.PlantDetailService;
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
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Administración")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;
    private final PlantDetailService plantDetailService;
    private final CalendarPlantService calendarPlantService;

    // ── Usuarios ─────────────────────────────────────────────────────────────

    @PostMapping("/users")
    @Operation(summary = "Crear usuario")
    public ResponseEntity<AdminUserResponse> createUser(
            @Valid @RequestBody AdminCreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createUser(request));
    }

    @GetMapping("/users")
    @Operation(summary = "Listar todos los usuarios")
    public ResponseEntity<List<AdminUserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Detalle de un usuario")
    public ResponseEntity<AdminUserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @PutMapping("/users/{id}/role")
    @Operation(summary = "Cambiar rol de un usuario")
    public ResponseEntity<AdminUserResponse> changeRole(
            @PathVariable Long id,
            @Valid @RequestBody ChangeRoleRequest request,
            @AuthenticationPrincipal UserDetails admin) {
        return ResponseEntity.ok(adminService.changeRole(id, request, admin.getUsername()));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Eliminar usuario y todos sus datos")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails admin) {
        adminService.deleteUser(id, admin.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    @Operation(summary = "Estadísticas globales de la aplicación")
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    // ── Fichas de plantas ──────────────────────────────────────────────────

    @PostMapping("/plant-details")
    @Operation(summary = "Crear ficha de planta")
    public ResponseEntity<PlantDetailResponse> createPlantDetail(
            @Valid @RequestBody PlantDetailRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(plantDetailService.create(request));
    }

    @PutMapping("/plant-details/{id}")
    @Operation(summary = "Actualizar ficha de planta")
    public ResponseEntity<PlantDetailResponse> updatePlantDetail(
            @PathVariable Long id,
            @Valid @RequestBody PlantDetailRequest request) {
        return ResponseEntity.ok(plantDetailService.update(id, request));
    }

    @DeleteMapping("/plant-details/{id}")
    @Operation(summary = "Eliminar ficha de planta")
    public ResponseEntity<Void> deletePlantDetail(@PathVariable Long id) {
        plantDetailService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── Plantas del calendario (BD) ────────────────────────────────────────

    @GetMapping("/calendar-plants")
    @Operation(summary = "Listar plantas del calendario añadidas por admin")
    public ResponseEntity<List<CalendarPlantAdminResponse>> getAllCalendarPlants() {
        return ResponseEntity.ok(calendarPlantService.findAll());
    }

    @PostMapping("/calendar-plants")
    @Operation(summary = "Añadir nueva planta al calendario")
    public ResponseEntity<CalendarPlantAdminResponse> createCalendarPlant(
            @Valid @RequestBody CalendarPlantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(calendarPlantService.create(request));
    }

    @PutMapping("/calendar-plants/{id}")
    @Operation(summary = "Actualizar planta del calendario")
    public ResponseEntity<CalendarPlantAdminResponse> updateCalendarPlant(
            @PathVariable Long id,
            @Valid @RequestBody CalendarPlantRequest request) {
        return ResponseEntity.ok(calendarPlantService.update(id, request));
    }

    @DeleteMapping("/calendar-plants/{id}")
    @Operation(summary = "Eliminar planta del calendario")
    public ResponseEntity<Void> deleteCalendarPlant(@PathVariable Long id) {
        calendarPlantService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
