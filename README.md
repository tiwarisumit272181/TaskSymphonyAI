# Task Tracker API

A RESTful Spring Boot API for managing projects, users, and tasks.

## Tech Stack

- Java 21
- Spring Boot 4.1.1 (Web, Data JPA, Validation)
- H2 in-memory database
- Maven
- JUnit 5 and Mockito
- Spring Boot Test and MockMvc
- springdoc-openapi (Swagger UI)
- Docker and Docker Compose

## Prerequisites

- JDK 21 or newer
- Maven 3.8+ (or use the included Maven wrapper)
- Docker (optional)

## Setup and Run

### Option 1: Maven

```bash
git clone https://github.com/tiwarisumit272181/task-tracker-api.git
mvn spring-boot:run
```

On Windows, you can use:

```powershell
.\mvnw.cmd spring-boot:run
```

### Option 2: Docker

```bash
docker compose up --build
```

The application starts at `http://localhost:8080`.

The database uses H2 in-memory storage, so all data is reset when the application restarts. No external database setup is required.

## Useful URLs

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Application info: `http://localhost:8080/api/info`

The API contract is available in `openapi.yaml` at the project root.

## Run Tests

```bash
mvn test
```

This runs the unit tests for services and pagination helpers, along with controller integration tests using MockMvc.

## Configuration

Application settings are defined in `src/main/resources/application.properties`.

| Property | Default | Description |
|---|---|---|
| `app.name` | Task Tracker API | Application name returned by `GET /api/info` |
| `app.version` | 1.0.0 | Application version returned by `GET /api/info` |
| `task.default-priority` | MEDIUM | Default priority for new tasks |
| `pagination.default-page-size` | 10 | Default number of records per page |

Values can be overridden using environment variables. For example:

```bash
TASK_DEFAULT_PRIORITY=HIGH
```

See `docker-compose.yml` for the Docker configuration.

## API Endpoints

### Application

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/info` | Get application name and version |

### Projects

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/projects` | Create a project |
| GET | `/api/projects` | List projects with pagination |
| GET | `/api/projects/{id}` | Get a project |
| PUT | `/api/projects/{id}` | Update a project |
| DELETE | `/api/projects/{id}` | Delete a project and its tasks |
| GET | `/api/projects/{id}/tasks` | List tasks belonging to a project |

### Users

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/users` | Create a user |
| GET | `/api/users` | List users with pagination |
| GET | `/api/users/{id}` | Get a user |
| PUT | `/api/users/{id}` | Update a user |
| DELETE | `/api/users/{id}` | Delete a user |

### Tasks

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/tasks` | Create a task |
| GET | `/api/tasks` | List tasks with optional filters |
| GET | `/api/tasks/{id}` | Get a task |
| PUT | `/api/tasks/{id}` | Update a task |
| DELETE | `/api/tasks/{id}` | Delete a task |

## Pagination and Sorting

All list endpoints support the following query parameters.

| Parameter | Default | Description |
|---|---|---|
| `page` | 0 | Zero-based page number |
| `size` | 10 | Number of records per page, between 1 and 100 |
| `sortBy` | `id` | Field used to sort the results |
| `direction` | `asc` | Sort direction: `asc` or `desc` |

Supported sorting fields:

- Projects: `id`, `name`, `createdAt`
- Users: `id`, `name`, `email`, `role`
- Tasks: `id`, `title`, `status`, `priority`, `dueDate`

Tasks can also be filtered using `status` and `projectId`.

Example request:

```http
GET /api/tasks?page=0&size=5&sortBy=title&direction=desc
```

Example response:

```json
{
  "content": [
    {
      "id": 1,
      "name": "Website Revamp"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

## Sample Requests

The following examples should be run in order. Task requests assume that project ID `1` and user ID `1` already exist.

The first project and user created receive these IDs in a fresh database.

### Linux, macOS, or Git Bash

**1. Get application information**

```bash
curl http://localhost:8080/api/info
```

**2. Create a project**

```bash
curl -X POST http://localhost:8080/api/projects \
  -H "Content-Type: application/json" \
  -d '{"name":"Website Revamp","description":"Q4 project"}'
```

**3. Create a user**

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@example.com","role":"DEVELOPER"}'
```

**4. Create a task**

The priority is omitted, so the configured default is used.

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Design homepage","projectId":1,"assigneeId":1,"dueDate":"2026-12-31"}'
```

Create another task with an explicit priority and status:

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Fix login bug","projectId":1,"priority":"HIGH","status":"IN_PROGRESS"}'
```

**5. Filter tasks**

```bash
curl "http://localhost:8080/api/tasks?status=TODO&projectId=1"
```

**6. Paginate and sort results**

```bash
curl "http://localhost:8080/api/tasks?page=0&size=5&sortBy=title&direction=desc"

curl "http://localhost:8080/api/projects?sortBy=createdAt&direction=desc