package com.vignesh.task_management_system.integration;

import com.vignesh.task_management_system.dto.CreateTaskRequest;
import com.vignesh.task_management_system.dto.TaskResponse;
import com.vignesh.task_management_system.dto.UpdateTaskRequest;
import com.vignesh.task_management_system.model.TaskStatus;
import com.vignesh.task_management_system.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskApiIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TaskRepository taskRepository;

    private RestClient restClient;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        restClient = RestClient.create("http://localhost:" + port);
    }

    @Test
    void shouldCreateAndRetrieveTask() {
        var request = new CreateTaskRequest("Integration task", "Test description", TaskStatus.PENDING, LocalDate.now().plusDays(5));

        var createResponse = restClient.post()
                .uri("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toEntity(TaskResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        var created = createResponse.getBody();
        assertThat(created.id()).isNotBlank();
        assertThat(created.title()).isEqualTo("Integration task");
        assertThat(created.description()).isEqualTo("Test description");
        assertThat(created.status()).isEqualTo(TaskStatus.PENDING);
        assertThat(created.createdAt()).isNotNull();
        assertThat(created.updatedAt()).isNotNull();

        var getResponse = restClient.get()
                .uri("/tasks/{id}", created.id())
                .retrieve()
                .toEntity(TaskResponse.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isEqualTo(created);
    }

    @Test
    void shouldReturn404ForNonExistentTask() {
        var response = restClient.get()
                .uri("/tasks/nonexistent-id")
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND, (req, res) -> {})
                .toBodilessEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldUpdateTask() {
        var createReq = new CreateTaskRequest("Before update", "Original", TaskStatus.PENDING, LocalDate.now().plusDays(10));
        var created = restClient.post()
                .uri("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .body(createReq)
                .retrieve()
                .body(TaskResponse.class);

        var updateReq = new UpdateTaskRequest("After update", "Modified", TaskStatus.IN_PROGRESS, LocalDate.now().plusDays(20));
        var updateResponse = restClient.put()
                .uri("/tasks/{id}", created.id())
                .contentType(MediaType.APPLICATION_JSON)
                .body(updateReq)
                .retrieve()
                .toEntity(TaskResponse.class);

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        var updated = updateResponse.getBody();
        assertThat(updated.title()).isEqualTo("After update");
        assertThat(updated.description()).isEqualTo("Modified");
        assertThat(updated.status()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void shouldPartiallyUpdateTask() {
        var createReq = new CreateTaskRequest("Original", "Desc", TaskStatus.PENDING, LocalDate.now().plusDays(7));
        var created = restClient.post()
                .uri("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .body(createReq)
                .retrieve()
                .body(TaskResponse.class);

        var updateReq = new UpdateTaskRequest("Updated title", null, null, null);
        var updated = restClient.put()
                .uri("/tasks/{id}", created.id())
                .contentType(MediaType.APPLICATION_JSON)
                .body(updateReq)
                .retrieve()
                .body(TaskResponse.class);

        assertThat(updated.title()).isEqualTo("Updated title");
        assertThat(updated.description()).isEqualTo("Desc");
        assertThat(updated.status()).isEqualTo(TaskStatus.PENDING);
    }

    @Test
    void shouldDeleteTask() {
        var createReq = new CreateTaskRequest("To be deleted", null, null, LocalDate.now().plusDays(3));
        var created = restClient.post()
                .uri("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .body(createReq)
                .retrieve()
                .body(TaskResponse.class);

        var deleteResponse = restClient.delete()
                .uri("/tasks/{id}", created.id())
                .retrieve()
                .toBodilessEntity();

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        var getResponse = restClient.get()
                .uri("/tasks/{id}", created.id())
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND, (req, res) -> {})
                .toBodilessEntity();

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturn404ForDeletingNonExistentTask() {
        var response = restClient.delete()
                .uri("/tasks/nonexistent")
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND, (req, res) -> {})
                .toBodilessEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnTasksSortedByDueDate() {
        var later = new CreateTaskRequest("Later task", null, null, LocalDate.now().plusDays(10));
        var earlier = new CreateTaskRequest("Earlier task", null, null, LocalDate.now().plusDays(1));
        var middle = new CreateTaskRequest("Middle task", null, null, LocalDate.now().plusDays(5));

        var laterTask = createTask(later);
        var earlierTask = createTask(earlier);
        var middleTask = createTask(middle);

        @SuppressWarnings("unchecked")
        var tasks = restClient.get()
                .uri("/tasks")
                .retrieve()
                .body(List.class);

        assertThat(tasks).hasSize(3);
    }

    @Test
    void shouldReturn400ForCreateWithBlankTitle() {
        var request = new CreateTaskRequest("", "desc", null, LocalDate.now().plusDays(1));

        var response = restClient.post()
                .uri("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .onStatus(status -> status == HttpStatus.BAD_REQUEST, (req, res) -> {})
                .toBodilessEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturn400ForCreateWithNullDueDate() {
        var request = new CreateTaskRequest("Title", "desc", null, null);

        var response = restClient.post()
                .uri("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .onStatus(status -> status == HttpStatus.BAD_REQUEST, (req, res) -> {})
                .toBodilessEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnEmptyListWhenNoTasks() {
        @SuppressWarnings("unchecked")
        var tasks = restClient.get()
                .uri("/tasks")
                .retrieve()
                .body(List.class);

        assertThat(tasks).isEmpty();
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistentTask() {
        var request = new UpdateTaskRequest("New title", null, null, null);

        var response = restClient.put()
                .uri("/tasks/nonexistent")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND, (req, res) -> {})
                .toBodilessEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldFilterTasksByStatus() {
        createTask(new CreateTaskRequest("Pending A", null, TaskStatus.PENDING, LocalDate.now().plusDays(1)));
        createTask(new CreateTaskRequest("In Progress", null, TaskStatus.IN_PROGRESS, LocalDate.now().plusDays(2)));
        createTask(new CreateTaskRequest("Pending B", null, TaskStatus.PENDING, LocalDate.now().plusDays(3)));

        @SuppressWarnings("unchecked")
        var tasks = restClient.get()
                .uri("/tasks?status=PENDING")
                .retrieve()
                .body(List.class);

        assertThat(tasks).hasSize(2);
    }

    @Test
    void shouldPaginateTasks() {
        for (int i = 0; i < 5; i++) {
            createTask(new CreateTaskRequest("Task " + i, null, null, LocalDate.now().plusDays(i + 1)));
        }

        @SuppressWarnings("unchecked")
        var result = restClient.get()
                .uri("/tasks?page=0&size=2")
                .retrieve()
                .body(Map.class);

        assertThat(result).containsKey("content");
        assertThat(result).containsKey("total_elements");
        assertThat(result).containsKey("page");
        assertThat(result.get("page")).isEqualTo(0);
        assertThat(result.get("size")).isEqualTo(2);
    }

    @Test
    void shouldFilterAndPaginateTasks() {
        createTask(new CreateTaskRequest("Pending", null, TaskStatus.PENDING, LocalDate.now().plusDays(1)));
        createTask(new CreateTaskRequest("In Progress", null, TaskStatus.IN_PROGRESS, LocalDate.now().plusDays(2)));
        createTask(new CreateTaskRequest("Done", null, TaskStatus.DONE, LocalDate.now().plusDays(3)));

        @SuppressWarnings("unchecked")
        var result = restClient.get()
                .uri("/tasks?status=DONE&page=0&size=10")
                .retrieve()
                .body(Map.class);

        assertThat(result).containsKey("content");
        @SuppressWarnings("unchecked")
        var content = (List<Map<String, Object>>) result.get("content");
        assertThat(content).hasSize(1);
        assertThat(content.get(0).get("title")).isEqualTo("Done");
    }

    private TaskResponse createTask(CreateTaskRequest request) {
        return restClient.post()
                .uri("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(TaskResponse.class);
    }
}
