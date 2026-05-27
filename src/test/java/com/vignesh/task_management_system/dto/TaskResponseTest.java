package com.vignesh.task_management_system.dto;

import com.vignesh.task_management_system.model.Task;
import com.vignesh.task_management_system.model.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TaskResponseTest {

    @Test
    void shouldMapFromTask() {
        var createdAt = LocalDateTime.of(2026, 5, 27, 10, 0);
        var updatedAt = LocalDateTime.of(2026, 5, 27, 12, 0);
        var dueDate = LocalDate.of(2026, 6, 15);
        var task = Task.builder()
                .id("task-1")
                .title("Test task")
                .description("A description")
                .status(TaskStatus.IN_PROGRESS)
                .dueDate(dueDate)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        var response = TaskResponse.from(task);

        assertThat(response.id()).isEqualTo("task-1");
        assertThat(response.title()).isEqualTo("Test task");
        assertThat(response.description()).isEqualTo("A description");
        assertThat(response.status()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(response.dueDate()).isEqualTo(dueDate);
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void shouldMapTaskWithNullDescription() {
        var dueDate = LocalDate.of(2026, 7, 1);
        var task = Task.create("No desc", null, null, dueDate);

        var response = TaskResponse.from(task);

        assertThat(response.description()).isNull();
        assertThat(response.status()).isEqualTo(TaskStatus.PENDING);
    }
}
