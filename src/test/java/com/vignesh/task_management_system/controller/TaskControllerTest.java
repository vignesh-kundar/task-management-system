package com.vignesh.task_management_system.controller;

import com.vignesh.task_management_system.dto.CreateTaskRequest;
import com.vignesh.task_management_system.dto.TaskResponse;
import com.vignesh.task_management_system.dto.UpdateTaskRequest;
import com.vignesh.task_management_system.exception.GlobalExceptionHandler;
import com.vignesh.task_management_system.exception.TaskNotFoundException;
import com.vignesh.task_management_system.model.Task;
import com.vignesh.task_management_system.model.TaskStatus;
import com.vignesh.task_management_system.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskService taskService;

    @TestConfiguration
    static class MockConfig {
        @Bean
        TaskService taskService() {
            return mock(TaskService.class);
        }
    }

    @BeforeEach
    void setUp() {
        reset(taskService);
    }

    @Test
    void shouldCreateTask() throws Exception {
        var dueDate = LocalDate.now().plusDays(1);
        var task = Task.create("Test task", "Description", TaskStatus.PENDING, dueDate);
        given(taskService.createTask(eq("Test task"), eq("Description"), eq(TaskStatus.PENDING), eq(dueDate)))
                .willReturn(task);

        var request = new CreateTaskRequest("Test task", "Description", TaskStatus.PENDING, dueDate);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(task.getId()))
                .andExpect(jsonPath("$.title").value("Test task"));
    }

    @Test
    void shouldReturn400WhenCreateRequestIsInvalid() throws Exception {
        var request = new CreateTaskRequest("", null, null, null);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetTaskById() throws Exception {
        var dueDate = LocalDate.now().plusDays(1);
        var task = Task.create("My task", null, null, dueDate);
        given(taskService.getTask(task.getId())).willReturn(task);

        mockMvc.perform(get("/tasks/{id}", task.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId()))
                .andExpect(jsonPath("$.title").value("My task"));
    }

    @Test
    void shouldReturn404WhenTaskNotFound() throws Exception {
        given(taskService.getTask("unknown")).willThrow(new TaskNotFoundException("unknown"));

        mockMvc.perform(get("/tasks/{id}", "unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateTask() throws Exception {
        var dueDate = LocalDate.now().plusDays(1);
        var task = Task.create("Original", null, null, dueDate);
        var updatedTask = task.toBuilder().title("Updated").build();
        given(taskService.updateTask(eq(task.getId()), eq("Updated"), any(), any(), any()))
                .willReturn(updatedTask);

        var request = new UpdateTaskRequest("Updated", null, null, null);

        mockMvc.perform(put("/tasks/{id}", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));
    }

    @Test
    void shouldDeleteTask() throws Exception {
        mockMvc.perform(delete("/tasks/{id}", "task-1"))
                .andExpect(status().isNoContent());
        verify(taskService).deleteTask("task-1");
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentTask() throws Exception {
        willThrow(new TaskNotFoundException("unknown")).given(taskService).deleteTask("unknown");

        mockMvc.perform(delete("/tasks/{id}", "unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldListAllTasks() throws Exception {
        var dueDate = LocalDate.now().plusDays(1);
        var task1 = Task.create("Task 1", null, null, dueDate);
        var task2 = Task.create("Task 2", null, null, dueDate);
        given(taskService.getAllTasks()).willReturn(List.of(task1, task2));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldReturnEmptyListWhenNoTasks() throws Exception {
        given(taskService.getAllTasks()).willReturn(List.of());

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
