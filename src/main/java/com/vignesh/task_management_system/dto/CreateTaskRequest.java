package com.vignesh.task_management_system.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import com.vignesh.task_management_system.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CreateTaskRequest(
        @NotBlank String title,
        String description,
        TaskStatus status,
        @NotNull LocalDate dueDate
) {}
