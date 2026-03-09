# Document Management System

## Overview

The Document Management System is a comprehensive solution designed to handle the asynchronous creation and lifecycle processing of business documents.

## 1. Technologies

- **Java 17+**
- **Spring Boot 3**
- **PostgreSQL**
- **gRPC**
- **Docker / Docker Compose**
- **Maven**
- **OpenAPI / Swagger**

---

## 2. Architecture Description

The architecture is divided into three main components:

1. **Document Management Service**: The core backend application that exposes the REST API, manages the persistent state of all documents, and orchestrates background jobs.
2. **Approval Registry gRPC Service**: An external consistency service that records approved documents. The Document Management Service communicates with it via gRPC using the Outbox pattern for eventual consistency.
3. **Document Generator Utility**: A standalone command-line application designed to load-test and populate the system by calling the Document Management Service API. It is designed to create many documents, so you can set the number of documents which will be created.

---

## 3. Document Management Service

This is the primary Spring Boot application. It exposes endpoints to create, read, update, and delete documents. More importantly, it manages asynchronous document processing through two dedicated background workers that run continuously within the service.

### API Endpoints

The service provides a comprehensive REST API. Below is a summary of the available endpoints:

| Method | Endpoint | Description |
|--------|----------|-------------|
| **POST** | `/api/v1/documents` | Create a new document. Automatically assigned a `DRAFT` status. |
| **GET** | `/api/v1/documents/{id}` | Retrieve a specific document by its ID. |
| **GET** | `/api/v1/documents` | Search documents using paginated and filterable criteria. |
| **POST** | `/api/v1/documents/{id}/submit` | Transition a document from `DRAFT` to `SUBMITTED`. |
| **POST** | `/api/v1/documents/{id}/approve` | Transition a document from `SUBMITTED` to `APPROVED`. |
| **GET** | `/api/v1/documents/{id}/history` | Fetch the lifecycle history of a specific document. |
| **POST** | `/api/v1/documents/submit/batch` | Synchronously submit a batch of documents. |
| **POST** | `/api/v1/documents/approve/batch` | Synchronously approve a batch of documents. |
| **POST** | `/api/v1/documents/batch/get` | Retrieve a list of documents by passing an array of IDs. |
| **POST** | `/api/v1/documents/{id}/approve/concurrency-check` | Approve with an optimistic locking concurrency check to test system behavior. |
| **POST** | `/api/v1/documents/batch/approve-jobs` | Create an asynchronous background job to approve a large batch of documents. Returns a `jobId`. |
| **GET** | `/api/v1/documents/batch-jobs/{jobId}` | Poll the status and progress of a specific asynchronous batch job. |
| **GET** | `/api/v1/documents/batch-jobs/{jobId}/items` | Retrieve the paginated items and individual statuses associated with a specific batch job. |

### Asynchronous Batch Jobs

For large batch operations (e.g., approving 5,000 documents at once), standard synchronous HTTP requests risk hitting timeout limits since processing can take more than a minute. 

To handle this reliably, the system implements an **Asynchronous Job Pattern**. When you submit a large batch (via `/batch/approve-jobs`), the system immediately accepts the request and returns a unique `jobId`. A background worker then processes the batch items asynchronously. Clients can use the `jobId` to poll the `/batch-jobs/{jobId}` endpoints and track the progress of the batch without holding open a long-lived HTTP connection.

### Background Workers

* **Submit Work worker**: Handles the initial submission phase. When a document is created (often in a `DRAFT` state), this worker asynchronously picks it up, validates its contents, and transitions it to a `SUBMITTED` state, ready for further processing.
* **Approve Order worker**: Responsible for processing the submitted documents by asynchronously changing their status to `APPROVED`. This ensures that complex approval logic runs independently of the user-facing API requests, allowing the system to scale and remain responsive.

### Outbox Pattern for Consistency

To ensure the remote `Approval Registry gRPC Service` is always synchronized with local state changes without relying on distributed transactions, the Document Management Service implements the **Outbox Pattern**. 
Whenever a document is successfully `APPROVED`, a corresponding event is saved to a local outbox table. A background process reliably polls this outbox and sends the events to the gRPC service, guaranteeing at-least-once delivery.

---

## 4. Approval Registry gRPC Service

The **Approval Registry gRPC Service** is an external gRPC-based microservice whose sole responsibility is to act as a global ledger of approved documents. 

When the Document Management Service successfully approves a document, it asynchronously forwards an approval record to this service. This service receives the record and stores it in its own independent database (`approval_registry_db`), creating a decoupled, highly available, and eventually consistent audit trail for all finalized properties across the distributed system.

---

## 5. Document Generator Utility

To test the system or simulate real-world traffic, the project includes the Document Generator Utility. Rather than manually creating documents, this command-line tool sends an automated stream of HTTP requests to the Document Management Service API based on parameters.

### Configuration Parameters

The generator loads default parameters from its internal `generator.properties` file but allows you to override them with the following CLI arguments:

- `--count=<N>`: Total number of documents to generate.
- `--base-url=<URL>`: The target Document Management Service API URL.
- `--delay-ms=<N>`: Delay in milliseconds between each document creation request.
- `--author-prefix=<String>`: The prefix string for the document author field.
- `--title-prefix=<String>`: The prefix string for the document title.
- `--number-prefix=<String>`: The prefix string for the document number.

You can set these arguments when running the utility directly via Java.

---

## Flow of Document Generation and Processing

1. **Generation**: The Document Generator Utility starts, reads its configuration (from `generator.properties` or CLI arguments), and issues `N` create requests to the API.
2. **Ingestion**: The Document Management Service API receives the requests and persists the new documents in an initial `DRAFT` state.
3. **Submission**: The **Submit Work worker** periodically polls the database for new drafts, processes their submission logic, and updates their state to `SUBMITTED`.
4. **Approval**: The **Approve Order worker** detects the newly submitted documents and asynchronously updates their status to `APPROVED`, completing the workflow.

---

## Getting Started

### Prerequisites
* Java 17+
* Maven
* Docker & Docker Compose

### Step 1: Start Infrastructure & gRPC Service

Before running any Java application, you must spin up the required PostgreSQL databases (`documents_db` and `approval_registry_db`) along with the `Approval Registry gRPC Service` itself. These are provided via Docker Compose.

1. Navigate to the project root directory.
2. Start the infrastructure in detached mode:
   ```bash
   docker-compose up -d
   ```
   *This starts the `documents-postgres` (5433), `approval-registry-postgres` (5434), and `approval-registry-grpc-service` (9090) containers.*

### Step 2: Running the Document Management Service

This core project runs natively rather than in Docker so it is easier to debug and develop.

1. Navigate to the service directory:
   ```bash
   cd document-management-service
   ```
2. Start the Spring Boot application:
   ```bash
   ./mvnw spring-boot:run
   ```
   *The service will connect to the local Docker databases and start on `http://localhost:8080`. Liquibase will automatically apply database migrations on startup.*

### Step 3: Running the Document Generator Utility

1. Ensure the Document Management Service is running locally.
2. Navigate to the generator directory:
   ```bash
   cd document-generator-cli
   ```
3. Build the utility (if it hasn't been built yet):
   ```bash
   mvn clean package
   ```
4. Run the utility, passing your desired configuration overrides (e.g., to create 50 documents):
   ```bash
   java -jar target/document-generator-cli-1.0-SNAPSHOT.jar --count=50
   ```

---

## Example Workflow

1. Start the **Document Management Service**. The console logs will immediately indicate that both the **Submit Work worker** and **Approve Order worker** have started and are polling for work.
2. Run the **Document Generator Utility** instructing it to generate 100 documents:
   ```bash
   java -jar document-generator-cli/target/document-generator-cli-1.0-SNAPSHOT.jar --count=100
   ```
3. The Document Management Service receives the incoming API calls and persists the documents.
4. Watch the service logs:
   - You will see the **Submit Work worker** processing the new batch of documents.
   - Shortly after, you will see the **Approve Order worker** triggering to handle the approval for the newly submitted documents.

### Step 4: Accessing Swagger UI

The project includes built-in OpenAPI Swagger documentation. It serves as an interactive GUI where you can explore endpoints, check required schemas, and execute live API requests directly from your browser.

Once the `Document Management Service` is running, you can access the Swagger UI by visiting:
[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
