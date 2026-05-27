package com.vignesh.task_management_system.service;

import com.vignesh.task_management_system.dto.PageResult;
import com.vignesh.task_management_system.exception.TaskNotFoundException;
import com.vignesh.task_management_system.model.Task;
import com.vignesh.task_management_system.model.TaskStatus;
import com.vignesh.task_management_system.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    @Override
    public Task createTask(String title, String description, TaskStatus status, LocalDate dueDate) {
        validateRequiredTitle(title);
        validateRequiredDueDate(dueDate);
        var task = Task.create(title, description, status, dueDate);
        return taskRepository.save(task);
    }

    @Override
    public Task getTask(String id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    @Override
    public Task updateTask(String id, String title, String description, TaskStatus status, LocalDate dueDate) {
        var existing = getTask(id);
        if (title != null) validateRequiredTitle(title);
        if (dueDate != null) validateRequiredDueDate(dueDate);
        var updated = existing.toBuilder()
                .title(title != null ? title : existing.getTitle())
                .description(description != null ? description : existing.getDescription())
                .status(status != null ? status : existing.getStatus())
                .dueDate(dueDate != null ? dueDate : existing.getDueDate())
                .updatedAt(LocalDateTime.now())
                .build();
        return taskRepository.save(updated);
    }

    @Override
    public void deleteTask(String id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        taskRepository.deleteById(id);
    }

    @Override
    public List<Task> getAllTasks() {
        return taskRepository.findAllSortedByDueDate();
    }

    @Override
    public PageResult<Task> getAllTasks(TaskStatus status, int page, int size) {
        var allMatching = status != null
                ? taskRepository.findAllByStatusSortedByDueDate(status)
                : taskRepository.findAllSortedByDueDate();
        var totalElements = (long) allMatching.size();
        var fromIndex = Math.min(page * size, allMatching.size());
        var toIndex = Math.min(fromIndex + size, allMatching.size());
        var content = allMatching.subList(fromIndex, toIndex);
        return PageResult.of(content, totalElements, page, size);
    }

    private void validateRequiredTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title must not be blank");
        }
    }

    private void validateRequiredDueDate(LocalDate dueDate) {
        if (dueDate == null) {
            throw new IllegalArgumentException("Due date must not be null");
        }
        if (dueDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Due date must be in the future");
        }
    }
}
