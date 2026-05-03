package com.huertoapp.dto.response;

import com.huertoapp.model.Task;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class TaskResponse {
    private Long id;
    private String title;
    private Task.TaskType type;
    private LocalDate dueDate;
    private Boolean completed;
    private String notes;
    private Long gardenId;
    private String gardenName;
    private Long cropId;
    private String cropName;
}
