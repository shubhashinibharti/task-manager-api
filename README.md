# Task Manager API
 
A production-ready REST API built with Spring Boot, inspired by real-world task management tools like Jira and Rally. Demonstrates senior-level backend engineering practices including JWT authentication, role-based authorization, and clean layered architecture.

**Live API:** `https://task-manager-api-production-26c0.up.railway.app`

---
 
## Tech Stack
 
| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5 |
| Security | Spring Security + JWT (JJWT) |
| Database | PostgreSQL 15 |
| ORM | Hibernate / Spring Data JPA |
| Containerization | Docker + Docker Compose |
| Build Tool | Maven |
| Utilities | Lombok |
 
---
 
## Features
 
- **JWT Authentication** — Stateless auth using signed tokens. No server-side sessions.
- **Role-based Access** — ADMIN sees all tasks across all users. USER sees only their own tasks.
- **Task Ownership** — ManyToOne relationship between Task and AppUser.
- **Full CRUD** — Create, Read, Update (PUT + PATCH), Delete with correct HTTP status codes.
- **Pagination** — `GET /tasks?page=0&size=10` using Spring Data Pageable.
- **Input Validation** — Field-level validation with meaningful 400 error messages.
- **DTO Pattern** — Clean API responses. Password never exposed in responses.
- **Structured Logging** — SLF4J logging across all service methods with INFO/WARN levels.
- **Docker Compose** — One command to start the entire stack locally.
- **Cloud Deployment** — Live on Railway with managed PostgreSQL.

---
 
## Architecture
 
```
HTTP Request
    ↓
Security Filter (JWT validation)
    ↓
Controller (HTTP handling)
    ↓
Service (Business logic + role checks)
    ↓
Repository (Database operations)
    ↓
PostgreSQL
```
 
---
 
## API Endpoints
 
### Auth (Public)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/auth/register` | Register a new user with role USER or ADMIN |
| POST | `/auth/login` | Login and receive JWT token |
 
### Tasks (Requires JWT Token)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/tasks?page=0&size=10` | Get paginated tasks (ADMIN gets all, USER gets own) |
| GET | `/tasks/{id}` | Get task by ID |
| POST | `/tasks` | Create a new task |
| PATCH | `/tasks/{id}` | Partial update (only sent fields updated) |
| PUT | `/tasks/{id}` | Full update (replaces all fields) |
| DELETE | `/tasks/{id}` | Delete task by ID |
 
---
 
## Getting Started
 
### Prerequisites
- Java 17+
- Docker Desktop
### Run Locally
 
1. Clone the repository
```bash
git clone https://github.com/shubhashinibharti/task-manager-api.git
cd task-manager-api/task-api
```
 
2. Copy the example properties file and fill in your values
```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```
 
3. Run the application (Docker Compose starts PostgreSQL automatically)
```bash
./mvnw spring-boot:run
```
 
App starts at `http://localhost:8080`
 
---
 
## Usage Example
 
### Register
```bash
POST /auth/register
{
  "username": "john",
  "password": "password123",
  "role": "USER"
}
```
 
### Login
```bash
POST /auth/login
{
  "username": "john",
  "password": "password123"
}
# Returns: { "token": "eyJhbGci..." }
```
 
### Create a Task
```bash
POST /tasks
Authorization: Bearer eyJhbGci...
 
{
  "title": "Fix login bug",
  "description": "Token expiry not handled",
  "status": "OPEN"
}
```
 
### Get Tasks with Pagination
```bash
GET /tasks?page=0&size=5
Authorization: Bearer eyJhbGci...
```
 
---
 
## Project Structure
 
```
src/main/java/com/taskmanager/task_api/
├── controller/       # HTTP request handling
│   ├── AuthController.java
│   └── TaskController.java
├── service/          # Business logic
│   ├── AuthService.java
│   └── TaskService.java
├── repository/       # Database operations
│   ├── TaskRepository.java
│   └── UserRepository.java
├── entity/           # JPA entities (DB tables)
│   ├── Task.java
│   └── AppUser.java
├── dto/              # API response objects
│   └── TaskResponse.java
├── security/         # JWT + Spring Security
│   ├── JwtUtil.java
│   ├── JwtAuthFilter.java
│   ├── SecurityConfig.java
│   └── CustomUserDetailsService.java
└── exception/        # Global error handling
    └── GlobalExceptionHandler.java
```
 
---
 
## Key Design Decisions
 
**Why JWT over sessions?**
Stateless auth scales horizontally — any server can verify a token without shared session storage.
 
**Why DTO pattern?**
Entities are internal database objects. DTOs control exactly what the API exposes — prevents accidental leaking of sensitive fields.

**Why service-level role checks instead of URL-level?**
`SecurityConfig` handles authentication (is the user logged in?). The service handles authorization logic (what data can they see?). This separation means role logic lives where business logic lives — easier to test and extend.

**Why pagination?**
Loading all records at once is a performance problem at scale. Spring Data's `Pageable` adds SQL `LIMIT`/`OFFSET` automatically.
 
**Why Docker Compose?**
Eliminates "works on my machine" — any developer clones the repo and runs one command to get a working database.

**Why SLF4J over System.out.println?**
SLF4J adds timestamps, severity levels, and class names. Logs are filterable and production-ready. `System.out.println` has none of these.

---
 
## Author
 
Shubhashini Bharti — [GitHub](https://github.com/shubhashinibharti)
