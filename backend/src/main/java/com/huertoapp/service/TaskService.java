package com.huertoapp.service;

import com.huertoapp.dto.request.TaskRequest;
import com.huertoapp.dto.response.TaskResponse;
import com.huertoapp.exception.ResourceNotFoundException;
import com.huertoapp.model.Crop;
import com.huertoapp.model.Garden;
import com.huertoapp.model.Task;
import com.huertoapp.model.User;
import com.huertoapp.repository.CropRepository;
import com.huertoapp.repository.GardenRepository;
import com.huertoapp.repository.TaskRepository;
import com.huertoapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final GardenRepository gardenRepository;
    private final CropRepository cropRepository;
    private final UserRepository userRepository;

    public List<TaskResponse> getTasks(Long gardenId, Boolean pending, String username) {
        User user = getUser(username);
        List<Task> tasks;
        if (gardenId != null) {
            gardenRepository.findByIdAndUserId(gardenId, user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Huerto", gardenId));
            tasks = taskRepository.findByGardenIdAndGardenUserId(gardenId, user.getId());
        } else if (Boolean.TRUE.equals(pending)) {
            tasks = taskRepository.findByGardenUserIdAndCompleted(user.getId(), false);
        } else {
            tasks = taskRepository.findByGardenUserId(user.getId());
        }
        return tasks.stream().map(this::toResponse).toList();
    }

    @Transactional
    public TaskResponse createTask(TaskRequest request, String username) {
        User user = getUser(username);
        Garden garden = gardenRepository.findByIdAndUserId(request.getGardenId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Huerto", request.getGardenId()));

        Crop crop = null;
        if (request.getCropId() != null) {
            crop = cropRepository.findById(request.getCropId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cultivo", request.getCropId()));
        }

        Task task = Task.builder()
                .title(request.getTitle())
                .type(request.getType())
                .dueDate(request.getDueDate())
                .completed(false)
                .notes(request.getNotes())
                .crop(crop)
                .garden(garden)
                .build();

        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request, String username) {
        User user = getUser(username);
        Task task = taskRepository.findByIdAndGardenUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tarea", id));
        task.setTitle(request.getTitle());
        task.setType(request.getType());
        task.setDueDate(request.getDueDate());
        task.setNotes(request.getNotes());
        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse markComplete(Long id, String username) {
        User user = getUser(username);
        Task task = taskRepository.findByIdAndGardenUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tarea", id));
        task.setCompleted(!task.getCompleted());
        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(Long id, String username) {
        User user = getUser(username);
        Task task = taskRepository.findByIdAndGardenUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tarea", id));
        taskRepository.delete(task);
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }

    private TaskResponse toResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .type(task.getType())
                .dueDate(task.getDueDate())
                .completed(task.getCompleted())
                .notes(task.getNotes())
                .gardenId(task.getGarden().getId())
                .gardenName(task.getGarden().getName())
                .cropId(task.getCrop() != null ? task.getCrop().getId() : null)
                .cropName(task.getCrop() != null ? task.getCrop().getPlantName() : null)
                .build();
    }
}
