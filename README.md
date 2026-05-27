# Task Management System

A simplified backend REST API for a Task Management System built with **Spring Boot 4.0.6** and **Java 21**, following **Domain-Driven Design (DDD)** principles and **Test-Driven Development (TDD)** practices.

---

## Table of Contents

- [Setup Guide](#setup-guide)
- [Architecture & Design](#architecture--design)
- [Data Model & Schema](#data-model--schema)
- [API Reference](#api-reference)
- [Error Handling](#error-handling)
- [Testing Strategy](#testing-strategy)
- [Tech Stack](#tech-stack)

---

## Setup Guide

### Prerequisites

- **Java 21+** — required by Spring Boot 4
- **Maven** 3.9+ (or use the included Maven wrapper `./mvnw`)

### Clone & Build

```sh
git clone <repo-url> task-management-system
cd task-management-system

# Compile (fast check)
./mvnw compile

# Run all 75+ tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=TaskControllerTest

# Package without tests
./mvnw package -DskipTests

# Start the server
./mvnw spring-boot:run
```

The server starts on `http://localhost:8080`.

### Verify It Works

```sh
curl http://localhost:8080/tasks
# → []
```

---

## Architecture & Design

### Layered DDD Structure

The application follows a strict Domain-Driven Design layout with four layers:

```
┌──────────────────────────────────────────────────┐
│                 Interfaces Layer                  │
│  controller/  dto/  exception/                   │
│  (HTTP concerns, JSON mapping, error handling)    │
├──────────────────────────────────────────────────┤
│              Application Layer                    │
│  service/  (TaskServiceImpl)                     │
│  (orchestration, validation, business rules)      │
├──────────────────────────────────────────────────┤
│               Domain Layer                        │
│  model/  repository/  (interfaces)               │
│  (entities, enums, repository contracts)          │
├──────────────────────────────────────────────────┤
│            Infrastructure Layer                   │
│  repository/  (InMemoryTaskRepository)           │
│  (data persistence, technical concerns)           │
└──────────────────────────────────────────────────┘
```

### Dependency Rule

Each layer depends **only** on the layer below it:
- Controller depends on Service (via interface)
- Service depends on Repository (via interface)
- Repository depends on Model

This makes every layer testable in isolation by mocking the layer below.

### Package Layout

```
src/main/java/com/vignesh/task_management_system/
├── TaskManagementSystemApplication.java    # Spring Boot entry point
├── model/
│   ├── Task.java                           # Aggregate root entity
│   └── TaskStatus.java                     # Enum (PENDING, IN_PROGRESS, DONE)
├── repository/
│   ├── TaskRepository.java                 # Interface
│   └── InMemoryTaskRepository.java         # ConcurrentHashMap impl
├── service/
│   ├── TaskService.java                    # Interface
│   └── TaskServiceImpl.java               # Business logic + validation
├── controller/
│   └── TaskController.java                 # REST endpoints
├── dto/
│   ├── CreateTaskRequest.java              # POST input
│   ├── UpdateTaskRequest.java             # PUT input
│   ├── TaskResponse.java                   # Output representation
│   └── PageResult.java                     # Paginated response wrapper
└── exception/
    ├── TaskNotFoundException.java
    └── GlobalExceptionHandler.java         # @RestControllerAdvice
```

### Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| **In-memory store** (no database) | Per requirements — `ConcurrentHashMap` provides thread-safe CRUD without external dependencies |
| **Interfaces for Repository & Service** | Enables mocking in unit tests; follows DDD "repository pattern" |
| **Snake_case JSON** | Matches PRD spec; `@JsonNaming(SnakeCaseStrategy)` on every DTO |
| **Lombok `toBuilder()`** | Clean partial updates — copy existing task, override fields, save |
| **Jakarta Bean Validation** | `@NotBlank` / `@NotNull` on DTOs; `@Valid` in controller; `MethodArgumentNotValidException` → 400 |
| **UUID string IDs** | Auto-generated, globally unique, no sequence dependency |
| **RestClient (not TestRestTemplate)** | Spring Boot 4 removed `TestRestTemplate`; `RestClient` is the replacement |

---

## Data Model & Schema

### Task Entity

Although the data lives in memory (not a database), the logical schema is:

| Field | Type | JSON Field | Required | Default | Description |
|-------|------|------------|----------|---------|-------------|
| `id` | `String` (UUID v4) | `id` | auto | — | Auto-generated unique identifier |
| `title` | `String` | `title` | yes | — | Task title (must not be blank) |
| `description` | `String` | `description` | no | `null` | Optional detailed description |
| `status` | `TaskStatus` enum | `status` | no | `PENDING` | One of: `PENDING`, `IN_PROGRESS`, `DONE` |
| `dueDate` | `LocalDate` (ISO 8601) | `due_date` | yes | — | Must be today or in the future |
| `createdAt` | `LocalDateTime` (ISO 8601) | `created_at` | auto | — | Timestamp of creation |
| `updatedAt` | `LocalDateTime` (ISO 8601) | `updated_at` | auto | — | Timestamp of last update |

### TaskStatus Enum

```java
public enum TaskStatus {
    PENDING,
    IN_PROGRESS,
    DONE
}
```

### In-Memory Storage

- **Data structure:** `ConcurrentHashMap<String, Task>`
- **Key:** Task ID (UUID string)
- **Thread safety:** Guaranteed by `ConcurrentHashMap` — all read/write operations are safe under concurrent access
- **Sorting:** Queries return tasks sorted by `dueDate` ascending
- **No persistence:** Data is lost when the application stops (per requirements)

### JSON Naming Convention

All fields use **snake_case** in JSON (`due_date`, `created_at`, `updated_at`) via `@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)` on every DTO.

---

## API Reference

### Create Task

Creates a new task and returns it with an auto-generated ID.

```
POST /tasks
```

**Request body:**

```json
{
  "title": "Buy groceries",
  "description": "Milk, eggs, bread",
  "status": "PENDING",
  "due_date": "2026-06-15"
}
```

`status` and `description` are optional. If omitted, `status` defaults to `PENDING`.

**Response — `201 Created`:**

```json
{
  "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "title": "Buy groceries",
  "description": "Milk, eggs, bread",
  "status": "PENDING",
  "due_date": "2026-06-15",
  "created_at": "2026-05-27T10:30:00",
  "updated_at": "2026-05-27T10:30:00"
}
```

**Errors:**
| Status | Condition |
|--------|-----------|
| `400 Bad Request` | `title` is blank or missing / `due_date` is null or in the past |

---

### Get Task

Retrieves a single task by its ID.

```
GET /tasks/{id}
```

**Response — `200 OK`:**

```json
{
  "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "title": "Buy groceries",
  "description": "Milk, eggs, bread",
  "status": "PENDING",
  "due_date": "2026-06-15",
  "created_at": "2026-05-27T10:30:00",
  "updated_at": "2026-05-27T10:30:00"
}
```

**Errors:**
| Status | Condition |
|--------|-----------|
| `404 Not Found` | No task with the given ID exists |

---

### Update Task

Partially updates an existing task. Only the fields included in the request body are changed; omitted fields retain their current values.

```
PUT /tasks/{id}
```

**Request body** (all fields optional):

```json
{
  "title": "Updated title",
  "status": "IN_PROGRESS"
}
```

**Response — `200 OK`:**

```json
{
  "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "title": "Updated title",
  "description": "Milk, eggs, bread",
  "status": "IN_PROGRESS",
  "due_date": "2026-06-15",
  "created_at": "2026-05-27T10:30:00",
  "updated_at": "2026-05-27T10:35:00"
}
```

**Errors:**
| Status | Condition |
|--------|-----------|
| `404 Not Found` | No task with the given ID exists |
| `400 Bad Request` | `title` is blank (if provided) / `due_date` is in the past (if provided) |

---

### Delete Task

Deletes a task by its ID.

```
DELETE /tasks/{id}
```

**Response — `204 No Content`** (no body).

**Errors:**
| Status | Condition |
|--------|-----------|
| `404 Not Found` | No task with the given ID exists |

---

### List All Tasks

Returns all tasks sorted by `due_date` ascending.

```
GET /tasks
```

**Response — `200 OK`:**

```json
[
  {
    "id": "...",
    "title": "Early task",
    "description": null,
    "status": "PENDING",
    "due_date": "2026-06-01",
    "created_at": "2026-05-27T10:00:00",
    "updated_at": "2026-05-27T10:00:00"
  },
  {
    "id": "...",
    "title": "Later task",
    "description": null,
    "status": "PENDING",
    "due_date": "2026-06-15",
    "created_at": "2026-05-27T10:05:00",
    "updated_at": "2026-05-27T10:05:00"
  }
]
```

---

### List with Filtering & Pagination (Bonus)

```
GET /tasks?status=IN_PROGRESS&page=0&size=5
```

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `status` | `TaskStatus` | no | — | Filter by status: `PENDING`, `IN_PROGRESS`, or `DONE` |
| `page` | `int` | only with `size` | — | Zero-based page number |
| `size` | `int` | only with `page` | — | Number of items per page |

**Behavior:**

| Query | Returns |
|-------|---------|
| `GET /tasks` | Flat JSON array of all tasks sorted by `due_date` |
| `GET /tasks?status=PENDING` | Flat JSON array filtered by status |
| `GET /tasks?page=0&size=5` | Paginated `PageResult` object |
| `GET /tasks?status=DONE&page=0&size=5` | Paginated + filtered `PageResult` |

**Paginated response — `200 OK`:**

```json
{
  "content": [
    {
      "id": "...",
      "title": "In progress task",
      "status": "IN_PROGRESS",
      "due_date": "2026-06-10",
      ...
    }
  ],
  "total_elements": 1,
  "total_pages": 1,
  "page": 0,
  "size": 5
}
```

---

## Error Handling

All errors return a consistent JSON body:

```json
{
  "error": "Task not found with id: f47ac10b-58cc-4372-a567-0e02b2c3d479"
}
```

Validation errors from `@Valid` return a list of field-specific messages:

```json
{
  "errors": [
    "title: must not be blank",
    "dueDate: must not be null"
  ]
}
```

| HTTP Status | Trigger |
|-------------|---------|
| `400 Bad Request` | `IllegalArgumentException` (service validation) or `MethodArgumentNotValidException` (bean validation) |
| `404 Not Found` | `TaskNotFoundException` |
| `500 Internal Server Error` | Any unhandled exception |

---

## Testing Strategy

The project uses **Test-Driven Development** — every feature was implemented test-first.

### Test Layers (75 tests in 8 classes)

| Layer | Test Class | Approach | Tests |
|-------|-----------|----------|-------|
| **Model** | `TaskTest` | Plain JUnit 5 — test builder, factory, defaults, equality | 7 |
| **Repository** | `InMemoryTaskRepositoryTest` | Plain JUnit 5 — test CRUD, sorting, filtering, thread safety | 14 |
| **Service** | `TaskServiceImplTest` | Mocked `TaskRepository`, verify validation and business rules | 18 |
| **Controller** | `TaskControllerTest` | `@WebMvcTest` with MockMvc, mocked `TaskService` | 12 |
| **Exception** | `GlobalExceptionHandlerTest` | Direct handler invocation with mock exceptions | 4 |
| **DTO** | `CreateTaskRequestTest`, `TaskResponseTest` | Validation + Jackson serialization/deserialization | 7 |
| **Integration** | `TaskApiIntegrationTest` | `@SpringBootTest(RANDOM_PORT)` + `RestClient`, real end-to-end | 14 |

### Running Tests

```sh
# All tests
./mvnw test

# Single test class
./mvnw test -Dtest=TaskServiceImplTest

# Single test method
./mvnw test -Dtest=TaskServiceImplTest#shouldCreateTask
```

---

## Tech Stack

| Component | Version / Library |
|-----------|-------------------|
| Framework | Spring Boot 4.0.6 / Spring Web MVC 7 |
| Language | Java 21 |
| Build | Maven 3.9.16 (wrapper) |
| JSON | Jackson 3.1.2 (`tools.jackson.*`) |
| Validation | Jakarta Validation 3.x + Hibernate Validator 9 |
| Boilerplate | Lombok 1.18.46 |
| Tests | JUnit 5, Mockito 5, AssertJ |
| Persistence | `ConcurrentHashMap` (in-memory) |
| Server | Apache Tomcat 11 (embedded) |

---

## Spring Boot 4 Notes

This project uses **Spring Boot 4.0.6** which has several API differences from Spring Boot 3.x:

- **Jackson 3.x** — imports use `tools.jackson.databind.*` instead of `com.fasterxml.jackson.databind.*`
- **`@WebMvcTest`** — located at `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest`
- **No `@MockBean` / `@MockitoBean`** — provide mock beans via `@TestConfiguration` + `@Bean`
- **No `TestRestTemplate`** — use Spring's `RestClient` for integration tests
- **`@LocalServerPort`** — still available at `org.springframework.boot.test.web.server.LocalServerPort`
