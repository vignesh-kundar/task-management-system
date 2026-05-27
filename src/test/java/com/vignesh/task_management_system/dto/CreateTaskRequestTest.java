package com.vignesh.task_management_system.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import com.vignesh.task_management_system.model.TaskStatus;
import static org.assertj.core.api.Assertions.assertThat;

class CreateTaskRequestTest {

    private static Validator validator;
    private static ObjectMapper objectMapper;

    @BeforeAll
    static void setUp() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldPassValidationWithValidRequest() {
        var request = new CreateTaskRequest("Valid title", "Desc", null, LocalDate.now().plusDays(1));
        var violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailWhenTitleIsBlank() {
        var request = new CreateTaskRequest("", "Desc", null, LocalDate.now().plusDays(1));
        var violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldFailWhenDueDateIsNull() {
        var request = new CreateTaskRequest("Title", "Desc", null, null);
        var violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldDeserializeFromSnakeCaseJson() throws Exception {
        var json = """
                {"title": "My Task", "description": "Details", "status": "PENDING", "due_date": "2026-06-15"}
                """;
        var request = objectMapper.readValue(json, CreateTaskRequest.class);

        assertThat(request.title()).isEqualTo("My Task");
        assertThat(request.description()).isEqualTo("Details");
        assertThat(request.status()).isEqualTo(TaskStatus.PENDING);
        assertThat(request.dueDate()).isEqualTo(LocalDate.of(2026, 6, 15));
    }

    @Test
    void shouldDeserializeWithMinimalFields() throws Exception {
        var json = """
                {"title": "Minimal", "due_date": "2026-07-01"}
                """;
        var request = objectMapper.readValue(json, CreateTaskRequest.class);

        assertThat(request.title()).isEqualTo("Minimal");
        assertThat(request.description()).isNull();
        assertThat(request.status()).isNull();
        assertThat(request.dueDate()).isEqualTo(LocalDate.of(2026, 7, 1));
    }
}
