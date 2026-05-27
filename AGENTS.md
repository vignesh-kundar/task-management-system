# AGENTS.md — Task Management System

## Stack
- Spring Boot 4.0.6, Java 21, Maven wrapper (`./mvnw`)
- **Jackson 3.x** — imports use `tools.jackson.*`, NOT `com.fasterxml.jackson.*`
- `@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)` on all DTOs
- In-memory store (`ConcurrentHashMap`), no database, no JPA
- Jakarta Validation + Hibernate Validator for `@Valid` / `@NotBlank` / `@NotNull`

## DDD Package Layout
```
model/         — Task, TaskStatus (entity + enum)
repository/    — TaskRepository (interface), InMemoryTaskRepository (impl)
service/       — TaskService (interface), TaskServiceImpl (impl + validation)
controller/    — TaskController (REST endpoints)
dto/           — CreateTaskRequest, UpdateTaskRequest, TaskResponse, PageResult
exception/     — TaskNotFoundException, GlobalExceptionHandler
```

## API
| Method | Path | Params | Returns |
|--------|------|--------|---------|
| POST | `/tasks` | `{title, description?, status?, due_date}` | 201 + Task JSON |
| GET | `/tasks/{id}` | — | 200 / 404 |
| PUT | `/tasks/{id}` | `{title?, description?, status?, due_date?}` | 200 / 404 |
| DELETE | `/tasks/{id}` | — | 204 / 404 |
| GET | `/tasks` | `?status=&page=&size=` | flat array (no pagination) or `PageResult` (with page/size) |

## Spring Boot 4 Quirks
- **`@WebMvcTest`** at `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest` (NOT `...autoconfigure.web.servlet`)
- **`@MockBean` / `@MockitoBean` removed** — provide mock beans via `@TestConfiguration` + `@Bean` instead
- **`TestRestTemplate` removed** — use `RestClient` for integration tests
- `@LocalServerPort` at `org.springframework.boot.test.web.server.LocalServerPort`

## Commands
```sh
./mvnw compile                       # fast compile check
./mvnw test                          # all tests (75 tests across 8 test classes)
./mvnw test -Dtest=TaskControllerTest # single test class
./mvnw spring-boot:run               # start on port 8080
```

## Test Structure
- `model/` — unit tests for Task model (Lombok builder, factory, defaults)
- `repository/` — unit tests for InMemoryTaskRepository (CRUD, sorting, filtering)
- `service/` — unit tests for TaskServiceImpl (mocked repository, validation)
- `controller/` — `@WebMvcTest` slice tests (mocked service, MockMvc)
- `exception/` — unit tests for GlobalExceptionHandler
- `integration/` — `@SpringBootTest(RANDOM_PORT)` end-to-end with RestClient

## Conventions
- All JSON uses `snake_case` for field names (via `@JsonNaming` on DTOs)
- `Task.create(...)` factory generates UUID, timestamps, defaults status to PENDING
- `toBuilder()` for partial updates on Task
- `IllegalArgumentException` for validation errors (→ 400)
- `TaskNotFoundException` for missing tasks (→ 404)
