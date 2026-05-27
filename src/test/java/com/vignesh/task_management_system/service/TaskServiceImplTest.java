package com.vignesh.task_management_system.service;

import com.vignesh.task_management_system.exception.TaskNotFoundException;
import com.vignesh.task_management_system.model.Task;
import com.vignesh.task_management_system.model.TaskStatus;
import com.vignesh.task_management_system.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskServiceImpl(taskRepository);
    }

    @Test
    void shouldCreateTask() {
        var dueDate = LocalDate.now().plusDays(1);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var task = taskService.createTask("Test title", "Description", TaskStatus.PENDING, dueDate);

        assertThat(task.getTitle()).isEqualTo("Test title");
        assertThat(task.getDescription()).isEqualTo("Description");
        assertThat(task.getStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(task.getDueDate()).isEqualTo(dueDate);
        assertThat(task.getId()).isNotBlank();
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void shouldThrowWhenTitleIsNull() {
        var dueDate = LocalDate.now().plusDays(1);
        assertThatThrownBy(() -> taskService.createTask(null, "desc", null, dueDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Title must not be blank");
        verifyNoInteractions(taskRepository);
    }

    @Test
    void shouldThrowWhenTitleIsBlank() {
        var dueDate = LocalDate.now().plusDays(1);
        assertThatThrownBy(() -> taskService.createTask("  ", "desc", null, dueDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Title must not be blank");
        verifyNoInteractions(taskRepository);
    }

    @Test
    void shouldThrowWhenDueDateIsNull() {
        assertThatThrownBy(() -> taskService.createTask("Title", "desc", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Due date must not be null");
        verifyNoInteractions(taskRepository);
    }

    @Test
    void shouldThrowWhenDueDateIsInPast() {
        var pastDate = LocalDate.now().minusDays(1);
        assertThatThrownBy(() -> taskService.createTask("Title", "desc", null, pastDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Due date must be in the future");
        verifyNoInteractions(taskRepository);
    }

    @Test
    void shouldGetTaskWhenFound() {
        var dueDate = LocalDate.now().plusDays(1);
        var task = Task.create("Test", null, null, dueDate);
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        var found = taskService.getTask(task.getId());

        assertThat(found).isEqualTo(task);
    }

    @Test
    void shouldThrowWhenTaskNotFound() {
        when(taskRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTask("unknown"))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("Task not found with id: unknown");
    }

    @Test
    void shouldUpdateAllFields() {
        var dueDate = LocalDate.now().plusDays(1);
        var task = Task.create("Original", "Original desc", TaskStatus.PENDING, dueDate);
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var newDueDate = LocalDate.now().plusDays(5);
        var updated = taskService.updateTask(
                task.getId(), "Updated", "Updated desc", TaskStatus.IN_PROGRESS, newDueDate);

        assertThat(updated.getTitle()).isEqualTo("Updated");
        assertThat(updated.getDescription()).isEqualTo("Updated desc");
        assertThat(updated.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(updated.getDueDate()).isEqualTo(newDueDate);
    }

    @Test
    void shouldPartiallyUpdateFields() {
        var dueDate = LocalDate.now().plusDays(1);
        var task = Task.create("Original", "Original desc", TaskStatus.PENDING, dueDate);
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var updated = taskService.updateTask(task.getId(), "Updated only title", null, null, null);

        assertThat(updated.getTitle()).isEqualTo("Updated only title");
        assertThat(updated.getDescription()).isEqualTo("Original desc");
        assertThat(updated.getStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(updated.getDueDate()).isEqualTo(dueDate);
    }

    @Test
    void shouldThrowWhenUpdatingNonExistentTask() {
        when(taskRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateTask("unknown", "New title", null, null, null))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("Task not found with id: unknown");
    }

    @Test
    void shouldDeleteTask() {
        when(taskRepository.existsById("task-id")).thenReturn(true);

        taskService.deleteTask("task-id");

        verify(taskRepository).deleteById("task-id");
    }

    @Test
    void shouldThrowWhenDeletingNonExistentTask() {
        when(taskRepository.existsById("unknown")).thenReturn(false);

        assertThatThrownBy(() -> taskService.deleteTask("unknown"))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("Task not found with id: unknown");
        verify(taskRepository, never()).deleteById(any());
    }

    @Test
    void shouldReturnAllTasksSorted() {
        var dueDate = LocalDate.now().plusDays(1);
        var task1 = Task.create("A", null, null, dueDate);
        var task2 = Task.create("B", null, null, dueDate);
        when(taskRepository.findAllSortedByDueDate()).thenReturn(List.of(task1, task2));

        var tasks = taskService.getAllTasks();

        assertThat(tasks).hasSize(2).containsExactly(task1, task2);
    }

    @Test
    void shouldReturnFilteredAndPaginatedTasks() {
        var dueDate = LocalDate.now().plusDays(1);
        var task1 = Task.create("A", null, TaskStatus.PENDING, dueDate);
        var task2 = Task.create("B", null, TaskStatus.IN_PROGRESS, dueDate);
        var task3 = Task.create("C", null, TaskStatus.PENDING, dueDate);
        when(taskRepository.findAllByStatusSortedByDueDate(TaskStatus.PENDING))
                .thenReturn(List.of(task1, task3));

        var result = taskService.getAllTasks(TaskStatus.PENDING, 0, 1);

        assertThat(result.content()).containsExactly(task1);
        assertThat(result.totalElements()).isEqualTo(2);
        assertThat(result.totalPages()).isEqualTo(2);
        assertThat(result.page()).isZero();
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    void shouldReturnAllTasksWhenStatusIsNull() {
        var dueDate = LocalDate.now().plusDays(1);
        var task1 = Task.create("A", null, null, dueDate);
        var task2 = Task.create("B", null, null, dueDate);
        when(taskRepository.findAllSortedByDueDate()).thenReturn(List.of(task1, task2));

        var result = taskService.getAllTasks(null, 0, 10);

        assertThat(result.content()).containsExactly(task1, task2);
        assertThat(result.totalElements()).isEqualTo(2);
    }

    @Test
    void shouldReturnEmptyPageWhenOffsetExceedsSize() {
        when(taskRepository.findAllByStatusSortedByDueDate(TaskStatus.DONE)).thenReturn(List.of());

        var result = taskService.getAllTasks(TaskStatus.DONE, 0, 10);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
    }

    @Test
    void shouldThrowWhenUpdateWithBlankTitle() {
        var dueDate = LocalDate.now().plusDays(1);
        var task = Task.create("Original", null, null, dueDate);
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.updateTask(task.getId(), "  ", null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Title must not be blank");
    }

    @Test
    void shouldThrowWhenUpdateWithPastDueDate() {
        var dueDate = LocalDate.now().plusDays(1);
        var task = Task.create("Original", null, null, dueDate);
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.updateTask(task.getId(), null, null, null, LocalDate.now().minusDays(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Due date must be in the future");
    }
}
