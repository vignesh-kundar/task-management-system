package com.vignesh.task_management_system.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class TaskTest {

    @Test
    void shouldCreateTaskWithAllFields() {
        var dueDate = LocalDate.of(2026, 6, 15);
        var task = Task.builder()
                .id("test-id")
                .title("Implement login")
                .description("Add OAuth2 authentication")
                .status(TaskStatus.IN_PROGRESS)
                .dueDate(dueDate)
                .build();

        assertThat(task.getId()).isEqualTo("test-id");
        assertThat(task.getTitle()).isEqualTo("Implement login");
        assertThat(task.getDescription()).isEqualTo("Add OAuth2 authentication");
        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(task.getDueDate()).isEqualTo(dueDate);
    }

    @Test
    void shouldDefaultStatusToPending() {
        var dueDate = LocalDate.of(2026, 6, 15);
        var task = Task.builder()
                .id("test-id")
                .title("Test task")
                .dueDate(dueDate)
                .build();

        assertThat(task.getStatus()).isEqualTo(TaskStatus.PENDING);
    }

    @Test
    void shouldCreateTaskWithoutDescription() {
        var dueDate = LocalDate.of(2026, 6, 15);
        var task = Task.builder()
                .id("test-id")
                .title("Minimal task")
                .dueDate(dueDate)
                .build();

        assertThat(task.getDescription()).isNull();
    }

    @Test
    void shouldCreateTaskViaFactoryMethod() {
        var dueDate = LocalDate.of(2026, 7, 1);
        var task = Task.create("Write docs", "Document the API", TaskStatus.DONE, dueDate);

        assertThat(task.getId()).isNotBlank();
        assertThat(task.getTitle()).isEqualTo("Write docs");
        assertThat(task.getDescription()).isEqualTo("Document the API");
        assertThat(task.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(task.getDueDate()).isEqualTo(dueDate);
        assertThat(task.getCreatedAt()).isNotNull();
        assertThat(task.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldGenerateUniqueIds() {
        var dueDate = LocalDate.of(2026, 8, 1);
        var task1 = Task.create("Task 1", null, null, dueDate);
        var task2 = Task.create("Task 2", null, null, dueDate);

        assertThat(task1.getId()).isNotEqualTo(task2.getId());
    }

    @Test
    void shouldDefaultToPendingWhenNullStatusPassed() {
        var dueDate = LocalDate.of(2026, 9, 1);
        var task = Task.create("Test", null, null, dueDate);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.PENDING);
    }

    @Test
    void shouldConsiderSameIdAsEqual() {
        var dueDate = LocalDate.of(2026, 10, 1);
        var task1 = Task.builder().id("same-id").title("A").dueDate(dueDate).build();
        var task2 = Task.builder().id("same-id").title("B").dueDate(dueDate).build();

        assertThat(task1).isEqualTo(task2);
        assertThat(task1.hashCode()).isEqualTo(task2.hashCode());
    }
}
