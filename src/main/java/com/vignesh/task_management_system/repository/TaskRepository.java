package com.vignesh.task_management_system.repository;

import com.vignesh.task_management_system.model.Task;
import com.vignesh.task_management_system.model.TaskStatus;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {
    Task save(Task task);
    Optional<Task> findById(String id);
    List<Task> findAllSortedByDueDate();
    List<Task> findAllByStatusSortedByDueDate(TaskStatus status);
    void deleteById(String id);
    boolean existsById(String id);
    void deleteAll();
}
