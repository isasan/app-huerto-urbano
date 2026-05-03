package com.huertoapp.controller;

import com.huertoapp.dto.request.TaskRequest;
import com.huertoapp.dto.response.TaskResponse;
import com.huertoapp.service.TaskService;
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
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tareas")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    @Operation(summary = "Obtener tareas (filtros: gardenId, pending)")
    public ResponseEntity<List<TaskResponse>> getTasks(
            @RequestParam(required = false) Long gardenId,
            @RequestParam(required = false) Boolean pending,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(taskService.getTasks(gardenId, pending, user.getUsername()));
    }

    @PostMapping
    @Operation(summary = "Crear tarea")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request,
                                                    @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.createTask(request, user.getUsername()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tarea")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long id,
                                                    @Valid @RequestBody TaskRequest request,
                                                    @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(taskService.updateTask(id, request, user.getUsername()));
    }

    @PatchMapping("/{id}/complete")
    @Operation(summary = "Marcar tarea como completada/pendiente")
    public ResponseEntity<TaskResponse> completeTask(@PathVariable Long id,
                                                      @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(taskService.markComplete(id, user.getUsername()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tarea")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id,
                                            @AuthenticationPrincipal UserDetails user) {
        taskService.deleteTask(id, user.getUsername());
        return ResponseEntity.noContent().build();
    }
}
