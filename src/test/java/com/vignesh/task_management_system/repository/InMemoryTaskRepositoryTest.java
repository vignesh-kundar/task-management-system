package com.vignesh.task_management_system.repository;

import com.vignesh.task_management_system.model.Task;
import com.vignesh.task_management_system.model.TaskStatus;
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

    @Test
    void shouldDeleteAllTasks() {
        repository.save(Task.create("A", null, null, LocalDate.of(2026, 6, 1)));
        repository.save(Task.create("B", null, null, LocalDate.of(2026, 6, 2)));
        repository.deleteAll();

        assertThat(repository.findAllSortedByDueDate()).isEmpty();
    }

    @Test
    void shouldFindTasksByStatus() {
        var task1 = Task.create("Pending A", null, TaskStatus.PENDING, LocalDate.of(2026, 6, 1));
        var task2 = Task.create("In Progress", null, TaskStatus.IN_PROGRESS, LocalDate.of(2026, 6, 2));
        var task3 = Task.create("Pending B", null, TaskStatus.PENDING, LocalDate.of(2026, 6, 3));
        var task4 = Task.create("Done", null, TaskStatus.DONE, LocalDate.of(2026, 6, 4));
        repository.save(task1);
        repository.save(task2);
        repository.save(task3);
        repository.save(task4);

        var pendingTasks = repository.findAllByStatusSortedByDueDate(TaskStatus.PENDING);
        assertThat(pendingTasks).containsExactly(task1, task3);

        var inProgressTasks = repository.findAllByStatusSortedByDueDate(TaskStatus.IN_PROGRESS);
        assertThat(inProgressTasks).containsExactly(task2);

        var doneTasks = repository.findAllByStatusSortedByDueDate(TaskStatus.DONE);
        assertThat(doneTasks).containsExactly(task4);
    }

    @Test
    void shouldReturnAllTasksWhenStatusIsNull() {
        var task1 = Task.create("A", null, TaskStatus.PENDING, LocalDate.of(2026, 6, 1));
        var task2 = Task.create("B", null, TaskStatus.DONE, LocalDate.of(2026, 6, 2));
        repository.save(task1);
        repository.save(task2);

        var all = repository.findAllByStatusSortedByDueDate(null);
        assertThat(all).containsExactly(task1, task2);
    }
}
