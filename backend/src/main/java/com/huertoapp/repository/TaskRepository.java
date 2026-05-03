package com.huertoapp.repository;

import com.huertoapp.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByGardenId(Long gardenId);
    List<Task> findByGardenUserId(Long userId);
    List<Task> findByGardenUserIdAndCompleted(Long userId, Boolean completed);
    List<Task> findByGardenUserIdAndDueDateBefore(Long userId, LocalDate date);
    List<Task> findByGardenIdAndGardenUserId(Long gardenId, Long userId);
    java.util.Optional<Task> findByIdAndGardenUserId(Long id, Long userId);
    List<Task> findByGardenUserIdAndDueDateAndCompleted(Long userId, LocalDate date, Boolean completed);
    long countByGardenUserIdAndCompleted(Long userId, Boolean completed);
    long countByGardenUserIdAndCompletedAndDueDateBefore(Long userId, Boolean completed, LocalDate date);
}
