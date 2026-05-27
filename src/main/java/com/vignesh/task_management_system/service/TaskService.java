package com.vignesh.task_management_system.service;

import com.vignesh.task_management_system.model.Task;
import com.vignesh.task_management_system.model.TaskStatus;

import java.time.LocalDate;
import java.util.List;

public interface TaskService {
    Task createTask(String title, String description, TaskStatus status, LocalDate dueDate);

    Task getTask(String id);

    Task updateTask(String id, String title, String description, TaskStatus status, LocalDate dueDate);

    void deleteTask(String id);

    List<Task> getAllTasks();
}
