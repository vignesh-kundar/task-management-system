package com.vignesh.task_management_system.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
@ToString
public class Task {
    private String id;
    private String title;
    private String description;
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Task create(String title, String description, TaskStatus status, LocalDate dueDate) {
        var now = LocalDateTime.now();
        return Task.builder()
                .id(UUID.randomUUID().toString())
                .title(title)
                .description(description)
                .status(status != null ? status : TaskStatus.PENDING)
                .dueDate(dueDate)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
