package com.huertoapp.service;

import com.huertoapp.dto.request.TaskRequest;
import com.huertoapp.exception.ResourceNotFoundException;
import com.huertoapp.model.Garden;
import com.huertoapp.model.Task;
import com.huertoapp.model.User;
import com.huertoapp.repository.CropRepository;
import com.huertoapp.repository.GardenRepository;
import com.huertoapp.repository.TaskRepository;
import com.huertoapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private GardenRepository gardenRepository;
    @Mock private CropRepository cropRepository;
    @Mock private UserRepository userRepository;

    private TaskService taskService;
    private User user;
    private Garden garden;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository, gardenRepository, cropRepository, userRepository);
        user = User.builder().id(1L).username("isabel").build();
        garden = Garden.builder().id(10L).user(user).build();
    }

    @Test
    void createTask_withCropOfAnotherUser_throwsNotFound() {
        TaskRequest request = new TaskRequest();
        request.setTitle("Regar");
        request.setType(Task.TaskType.RIEGO);
        request.setGardenId(10L);
        request.setCropId(99L);

        when(userRepository.findByUsername("isabel")).thenReturn(Optional.of(user));
        when(gardenRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(garden));
        when(cropRepository.findByIdAndPlotGardenUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createTask(request, "isabel"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(taskRepository, never()).save(any());
    }
}
