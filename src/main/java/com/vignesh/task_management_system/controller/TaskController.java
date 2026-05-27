package com.vignesh.task_management_system.controller;

import com.vignesh.task_management_system.dto.CreateTaskRequest;
import com.vignesh.task_management_system.dto.TaskResponse;
import com.vignesh.task_management_system.dto.UpdateTaskRequest;
import com.vignesh.task_management_system.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        var task = taskService.createTask(
                request.title(), request.description(), request.status(), request.dueDate());
        return ResponseEntity.status(HttpStatus.CREATED).body(TaskResponse.from(task));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTask(@PathVariable String id) {
        var task = taskService.getTask(id);
        return ResponseEntity.ok(TaskResponse.from(task));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable String id, @Valid @RequestBody UpdateTaskRequest request) {
        var task = taskService.updateTask(
                id, request.title(), request.description(), request.status(), request.dueDate());
        return ResponseEntity.ok(TaskResponse.from(task));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> listTasks() {
        var tasks = taskService.getAllTasks().stream()
                .map(TaskResponse::from)
                .toList();
        return ResponseEntity.ok(tasks);
    }
}
