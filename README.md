# Document Management System (Test Assignment)

## Brief Description
The project is a server-side application for managing the document lifecycle. It supports `DRAFT`, `SUBMITTED`, and `APPROVED` statuses. The system implements transactional single and batch status transfer operations, parallel editing protection (Concurrency Check), as well as background processing of documents by scheduled workers (from `DRAFT` to `APPROVED`).

A separate Java CLI utility (`document-generator-cli`) is included for the rapid batch generation of test data.

## Implemented Features
- Document Management (Create, Read, Search).
- Lifecycle transitions: single `submit` and `approve`.
- Batch processing: `batch submit` and `batch approve` with "Partial Success" handling without transaction interruption due to single errors.
- History tracking: `Approval Registry` and `Document History` for status audit.
- Concurrency Check API (protection against writing outdated versions).
- Background workers:
  - `submit-worker` (automatic `DRAFT` -> `SUBMITTED`)
  - `approve-worker` (automatic `SUBMITTED` -> `APPROVED`)
- CLI Generator utility for load testing (via HTTP API).

## Technologies
- Java 21
- Spring Boot 3
- Spring Data JPA / Hibernate
- PostgreSQL (via Docker Compose)
- Liquibase (Automatic DB migrations)
- OpenAPI / Swagger (Interactive documentation)
- Maven
- Docker / Docker Compose

## Project Structure
```
/document-management-system
├── /document-management-service     # Main Spring Boot backend
├── /document-generator-cli          # Independent Java CLI for data generation
└── docker-compose.yml               # PostgreSQL DB 
```

## How to Run

### 1. Start the DB
From the project root, start the PostgreSQL database:
```bash
docker compose up -d
```

### 2. Start the Service
Navigate to the backend directory and launch the application via the maven wrapper:
```bash
cd document-management-service
./mvnw spring-boot:run
```
*Note: Liquibase will automatically apply schemas on startup.*

### 3. Swagger
Interactive documentation is available at:
[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

## Configuration
Key settings for `document-management-service` (`application.yml`):
- Database: URL `jdbc:postgresql://localhost:5433/documents_db`.
- Background Workers:
  ```yaml
  app:
    workers:
      enabled: true
      batch-size: 20
      submit-interval-ms: 10000
      approve-interval-ms: 15000
  ```

## How to Run the CLI Generator

The generator is packaged as an independent fat JAR.

**1. How to build:**
```bash
cd document-generator-cli
mvn clean package
```

**2. How to run:**
Launch the generation of $N$ documents via the `--count` configuration argument:
```bash
java -jar target/document-generator-cli-1.0-SNAPSHOT-shaded.jar --count=50
```

## API Examples / Verification Scenario

**Scenario:**
1. Start the DB and launch the service.
2. Generate data via the CLI: `java -jar document-generator-cli/target/document-generator-cli-1.0-SNAPSHOT-shaded.jar --count=20`.
3. Open `app.log` or the Spring Boot console: you will see how the `submit-worker` picks up `DRAFT`s in batches and transitions them to `SUBMITTED`. Then the `approve-worker` will pick them up and transition them to `APPROVED`.
4. Open the Swagger UI and request `GET /api/v1/documents`. You will see the created documents with the `APPROVED` status.

**API Test Examples (Swagger / Curl):**
- **Concurrency Check Test**: Try to send a `PUT /api/v1/documents/{id}` with `version: 0` when the actual document version in the DB is already `1`. You will receive an HTTP 409 Conflict.
- **Batch Submit/Approve Endpoints**: Pass a list of UUIDs to `/api/v1/documents/submit/batch`. If the 1st ID is correct, and the 2nd is erroneous (not found or already `SUBMITTED`), the service will return 200 OK (Partial Support) with a summary `total=2, success=1, error=1`.
- **Batch Get Paginated Endpoint**: POST `/api/v1/documents/batch/get?page=0&size=10&sortBy=title&sortDir=asc`. Returns documents by the provided pool of UUIDs, supporting pagination and safe sorting only by `title` and `createdAt` (otherwise returns `400 Bad Request`).

## Logging

The logging is structured in a minimalistic format without unnecessary spam.
- **CLI**: Outputs `[generator] creating document 1/N` and a final summary `[generator] finished: requested=X, success=Y, failed=Z, tookMs=...`.
- **Workers**: Show the start of the iteration, a short sample of UUIDs (first 3), and a summary of the batch results `[worker] batch result: total=X, success=Y, error=Z, tookMs=...`. If the database is empty, spam is suppressed with the message `no documents found`.

## Limitations / Assumptions
- Local execution (test assignment) without external S3 buckets for storing binary data, only metadata is stored.
- Authorization is not provided (open API) to focus on transactional security.
- An external Message Broker (Kafka/RabbitMQ) for Background Workers was not used; a native schedule-architecture based on the DB was implemented for a Zero-Dependency project launch.
