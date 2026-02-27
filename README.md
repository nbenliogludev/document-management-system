# Document Management System (Test Assignment)

## Brief Description
The project is a small distributed system for managing the document lifecycle. It consists of a REST service, a gRPC consistency service, asynchronous delivery mechanisms, and Saga-style rollbacks. It supports `DRAFT`, `SUBMITTED`, and `APPROVED` statuses. The system implements transactional single and batch status transfer operations, parallel editing protection (Concurrency Check), and background processing of documents by scheduled workers.

A separate Java CLI utility (`document-generator-cli`) is included for the rapid batch generation of test data.

## Architecture Overview
The system consists of two independent services and shared infrastructure:
- **Document Management Service**: The main Spring Boot REST API.
- **Approval Registry gRPC Service**: An external consistency service.

Two separate PostgreSQL databases are used:
- `documents_db`: Used exclusively by the Document Management Service.
- `approval_registry_db`: Used exclusively by the gRPC Approval Registry.

**Interaction Flow:**
1. A document is approved in the Document Management Service.
2. An approval event is sent via the **Outbox Pattern** for eventual consistency.
3. The Outbox Worker delivers the event to the external gRPC Approval Registry.
4. If the gRPC registry write fails permanently, a **Compensation Worker** triggers a Saga-style rollback, reverting the document's state safely from `APPROVED` back to `SUBMITTED`. This restores system consistency when the remote registry write completes with permanent failure.

## Implemented Features
- Document Management (Create, Read, Search).
- Lifecycle transitions: single `submit` and `approve`.
- Batch processing: `batch submit` and `batch approve` with "Partial Success" handling without transaction interruption due to single errors.
- History tracking: `Approval Registry` and `Document History` for status audit.
- Concurrency Check API (protection against writing outdated versions).
- Background workers:
  - `submit-worker` (automatic `DRAFT` -> `SUBMITTED`)
  - `approve-worker` (automatic `SUBMITTED` -> `APPROVED`)
  - `outbox-worker` (eventual consistency event publisher for the Approval Registry)
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
```text
document-management-system
├── document-management-service     # Main REST API (run locally)
├── approval-registry-grpc-service  # gRPC approval registry (runs in Docker)
├── document-generator-cli          # Load generator
└── docker-compose.yml              # Infra (Postgres + gRPC)
```

## Runtime Model

### Step 1: Start Infrastructure & gRPC Service
From the project root, start the PostgreSQL databases and the gRPC application container:
```bash
docker compose up -d
```
*This will spin up `documents-postgres` (5433), `approval-registry-postgres` (5434), and the `approval-registry-grpc-service` container (9090).*

**Note:** The Document Management Service is **NOT** run via `docker-compose` intentionally. It should be started manually locally to allow for direct debugging, live development, and simulating real distributed service-to-service interactions.

### Step 2: Start the Main API Service
Navigate to the backend directory and launch the application via the maven wrapper:
```bash
cd document-management-service
./mvnw spring-boot:run
```
*Note: Liquibase will automatically apply schemas on startup.*

### Step 3 (Optional): Build and Run the Load Generator
The generator is packaged as an independent fat JAR.
First, build it:
```bash
cd document-generator-cli
mvn clean package
```
Launch the generation of documents via the `--count` configuration argument:
```bash
java -jar target/document-generator-cli-1.0-SNAPSHOT-shaded.jar --count=50
```

### Swagger
Interactive documentation is available at:
[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

## Consistency & Compensation

Approval is handled asynchronously via the Outbox Pattern to guarantee eventual consistency between the local database and the remote registry. Failure of the remote registry triggers a retry network for robust redelivery. If the failure is permanent, a compensation worker restores global consistency by safely reverting the document from `APPROVED` back to `SUBMITTED`. This ensures no distributed inconsistency remains.

### Automatic Saga Compensation (Outbox)

If the `ApprovalRegistry` gRPC service permanently fails to store a new `APPROVED` document state (i.e. reaches its max-retry limit), the system enacts a localized Saga compensating action instead of a silent distributed failure.

A scheduled worker (`ApprovalRegistryCompensationWorker`) parses events mapped uniquely as `FAILED_PERMANENT`. The worker then re-inspects the document, and provided the document hasn't been reverted or modified externally, swaps its state from `APPROVED` safely back to `SUBMITTED`, preserving history alongside robust Optimistic Locks.

**To Simulate a Permanent Failure**
1. Stop the external gRPC service container:
   `docker-compose stop approval-registry-grpc-service`
2. Create and Submit a document normally via the REST API
3. Call the Approve Endpoint. The document will change to `APPROVED` globally.
4. Watch the `document-management-service` logs. After several retry warnings mapping `FAILED`, a terminal `FAILED_PERMANENT` triggers.
5. Notice the automatic compensator swap the document back to `SUBMITTED`, printing an `APPROVAL_REVERTED_REGISTRY_FAILED` trace under the Document History endpoints.

## Configuration
Key settings for `document-management-service` (`application.yml`):
- Database: URL `jdbc:postgresql://localhost:5433/documents_db`.
- Approval Registry Mode (`app.approval-registry.mode`): `local` (uses local DB) or `grpc` (calls the external remote gRPC service via port 9090).
- Background Workers:
  app:
    workers:
      enabled: true
      batch-size: 20
      submit-interval-ms: 10000
      approve-interval-ms: 15000
    outbox:
      enabled: true
      batch-size: 50
      polling-interval-ms: 5000
      max-retries: 3
      retry-backoff-ms: 2000
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

## Test Coverage

The system includes integration-level unit tests covering required business scenarios:

| Requirement | Test |
|------------|------|
| Happy-path single approval | BatchJobWorkerTest.processJobs_ShouldProcessSuccessfullyAndCompleteJob |
| Batch submit | BatchJobServiceTest.createApproveJob_ShouldDeduplicateAndSave |
| Batch approve with partial results | BatchJobWorkerTest.processJobs_ShouldMarkFailedItemsAndPartialSuccessJob |
| Approval rollback on registry failure | DocumentApprovalCompensationServiceTest.compensateApprovalRegistryFailure_ShouldCompensateSuccessfully_WhenDocumentApprovedAndRegistryMissing |
| Compensation retry / failure handling | ApprovalRegistryCompensationWorkerTest.processCompensations_ShouldUpdateStatus_WhenCompensationSucceeds |

Run all tests:

mvn test
