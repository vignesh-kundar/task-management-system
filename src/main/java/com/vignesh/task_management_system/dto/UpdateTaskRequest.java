package com.vignesh.task_management_system.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import com.vignesh.task_management_system.model.TaskStatus;

import java.time.LocalDate;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record UpdateTaskRequest(
        String title,
        String description,
        TaskStatus status,
        LocalDate dueDate
) {}
