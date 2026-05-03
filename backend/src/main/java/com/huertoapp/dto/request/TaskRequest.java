package com.huertoapp.dto.request;

import com.huertoapp.model.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskRequest {
    @NotBlank
    private String title;
    @NotNull
    private Task.TaskType type;
    private LocalDate dueDate;
    private Boolean completed;
    private String notes;
    private Long cropId;
    @NotNull
    private Long gardenId;
}
