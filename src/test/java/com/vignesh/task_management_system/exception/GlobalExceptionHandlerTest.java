package com.vignesh.task_management_system.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldReturn404ForTaskNotFound() {
        var ex = new TaskNotFoundException("task-1");
        var response = handler.handleTaskNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("error", "Task not found with id: task-1");
    }

    @Test
    void shouldReturn400ForIllegalArgument() {
        var ex = new IllegalArgumentException("Title must not be blank");
        var response = handler.handleIllegalArgument(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("error", "Title must not be blank");
    }

    @Test
    void shouldReturn400ForValidationErrors() {
        var bindingResult = mock(BindingResult.class);
        var fieldError = new FieldError("object", "title", "must not be blank");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        var ex = new MethodArgumentNotValidException(null, bindingResult);

        var response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsKey("errors");
        assertThat(response.getBody().get("errors").toString()).contains("title: must not be blank");
    }

    @Test
    void shouldReturn500ForGenericException() {
        var ex = new RuntimeException("Something broke");
        var response = handler.handleGeneral(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("error", "An unexpected error occurred");
    }
}
