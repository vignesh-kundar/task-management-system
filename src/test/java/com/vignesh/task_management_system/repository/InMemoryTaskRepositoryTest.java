package com.vignesh.task_management_system.repository;

import com.vignesh.task_management_system.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryTaskRepositoryTest {

    private InMemoryTaskRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTaskRepository();
    }

    @Test
    void shouldSaveAndFindTask() {
        var task = Task.create("Test task", "Description", null, LocalDate.of(2026, 6, 15));
        repository.save(task);

        var found = repository.findById(task.getId());
        assertThat(found).isPresent().contains(task);
    }

    @Test
    void shouldReturnEmptyWhenNotFound() {
        assertThat(repository.findById("nonexistent")).isEmpty();
    }

    @Test
    void shouldUpdateExistingTaskOnSave() {
        var task = Task.create("Original", null, null, LocalDate.of(2026, 6, 1));
        repository.save(task);

        var updated = task.toBuilder().title("Updated").build();
        repository.save(updated);

        assertThat(repository.findById(task.getId()))
                .isPresent()
                .hasValueSatisfying(t -> assertThat(t.getTitle()).isEqualTo("Updated"));
    }

    @Test
    void shouldReturnAllTasksSortedByDueDate() {
        var task1 = Task.create("Late", null, null, LocalDate.of(2026, 7, 1));
        var task2 = Task.create("Early", null, null, LocalDate.of(2026, 6, 1));
        var task3 = Task.create("Middle", null, null, LocalDate.of(2026, 6, 15));
        repository.save(task1);
        repository.save(task2);
        repository.save(task3);

        var tasks = repository.findAllSortedByDueDate();
        assertThat(tasks).containsExactly(task2, task3, task1);
    }

    @Test
    void shouldReturnEmptyListWhenNoTasks() {
        assertThat(repository.findAllSortedByDueDate()).isEmpty();
    }

    @Test
    void shouldDeleteTaskById() {
        var task = Task.create("Delete me", null, null, LocalDate.of(2026, 6, 15));
        repository.save(task);
        repository.deleteById(task.getId());

        assertThat(repository.findById(task.getId())).isEmpty();
    }

    @Test
    void shouldDeleteNonExistentTaskGracefully() {
        repository.deleteById("nonexistent");
        assertThat(repository.findAllSortedByDueDate()).isEmpty();
    }

    @Test
    void shouldReturnTrueIfTaskExists() {
        var task = Task.create("Exists", null, null, LocalDate.of(2026, 6, 15));
        repository.save(task);

        assertThat(repository.existsById(task.getId())).isTrue();
    }

    @Test
    void shouldReturnFalseIfTaskDoesNotExist() {
        assertThat(repository.existsById("nonexistent")).isFalse();
    }
}
