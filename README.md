# WhistleDrop — Speak Without Being Seen

WhistleDrop is a confidential reporting backend built with Spring Boot and PostgreSQL. It allows anyone to securely submit reports (e.g. security vulnerabilities, harassment, corruption, technical issues) completely anonymously—without registering an account, providing an email, or exposing personal identity.

Upon submitting a report, the whistleblower receives a cryptographically secure, random case code (e.g. `WD-X7K9P2M4Q8T6`). The backend stores only a one-way cryptographic hash (SHA-256) of this case code. The reporter can later use this code to track the status of their report and view progress updates.

Moderators log in using a secured API with JWT authentication to review reports, filter them, inspect details, and transition report statuses across a strictly enforced workflow.

---

## 1. Project Overview

In traditional reporting systems, users are often required to sign in with an institutional email, phone number, or student ID. Even when "anonymous mode" is advertised, systems often store user IDs, session cookies, or device fingerprints in their databases.

WhistleDrop was designed from the ground up with a **Zero-Identity Architecture**:
- There is **no reporter user model or registration**.
- No personal data (name, email, phone number, student ID, IP address, device headers) is ever captured or persisted.
- Access to submitted reports is controlled purely via knowledge of the secret case code.
- Only hashed case codes are stored in the database, preventing anyone with direct database access from impersonating reporters or correlating reports.

---

## 2. Features

- **Anonymous Submission**: Submit reports with category, description, and optional evidence URL without any login.
- **Cryptographic Case Codes**: Automatically generates unique, unguessable case codes (`WD-` followed by 12 random characters).
- **Hashed Case Code Verification**: Only SHA-256 hashes of case codes are stored in the database.
- **Case Tracking**: Anonymous reporters can track report status and review moderator timeline updates using their case code.
- **Moderator Authentication**: JWT-based login for authorized moderators with BCrypt password hashing.
- **Moderator Report Management**: Authenticated endpoints to list reports, filter by status and category, view individual reports, and post status updates.
- **Strict Status Workflow**: Enforces business logic transitions (`SUBMITTED` &rarr; `UNDER_REVIEW` &rarr; `RESOLVED` or `DISMISSED`).
- **Interactive Swagger / OpenAPI Documentation**: Full API explorer and testing interface out of the box.
- **Input Validation & Clean Error Handling**: Spring Bean Validation with simple, readable JSON error responses.

---

## 3. Tech Stack

- **Language**: Java 17+ (tested with Java 17 & Java 25)
- **Framework**: Spring Boot 3.3.4
  - Spring Web (RESTful APIs)
  - Spring Data JPA (Data persistence)
  - Spring Security (Authorization & route protection)
  - Bean Validation (`jakarta.validation`)
- **Database**: PostgreSQL (Production/Local), H2 (In-memory for isolated automated tests)
- **Security & Tokens**: JJWT (Java JWT 0.12.6), BCrypt Password Encoder
- **API Documentation**: Springdoc OpenAPI / Swagger UI 2.6.0
- **Build Tool**: Apache Maven

---

## 4. Setup & Running the Project

### Prerequisites
- Java 17 or higher (`java -version`)
- Maven 3.8+ (`mvn -version`)
- PostgreSQL installed and running (or use Docker)

### Step 1: Database Setup
Log into your PostgreSQL instance and create the `whistledrop` database:

```sql
CREATE DATABASE whistledrop;
```

### Step 2: Configure Environment Variables
You can copy `.env.example` to `.env` or export environment variables in your terminal:

```bash
cp .env.example .env
```

Environment variables supported:
| Variable | Description | Default Value |
|---|---|---|
| `DB_URL` | PostgreSQL JDBC connection URL | `jdbc:postgresql://localhost:5432/whistledrop` |
| `DB_USERNAME` | PostgreSQL username | `postgres` |
| `DB_PASSWORD` | PostgreSQL password | `postgres` |
| `JWT_SECRET` | Secret key for signing JWTs (min 32 chars) | Built-in development key |
| `PORT` | Server listening port | `8080` |

### Step 3: Run the Application
Use Maven to start the Spring Boot application:

```bash
mvn spring-boot:run
```

Alternatively, you can build an executable JAR and run it:

```bash
mvn clean package
java -jar target/whistledrop-1.0.0.jar
```

The application will start on `http://localhost:8080`.

### Initial Moderator Account
On first boot, the application automatically seeds an initial default moderator account if one does not already exist:
- **Username**: `moderator`
- **Password**: `password`

---

## 5. API Documentation

Swagger UI is available at:
`http://localhost:8080/swagger-ui.html`

OpenAPI specification is available at:
`http://localhost:8080/v3/api-docs`

---

### Anonymous Endpoints (Public)

#### 1. Submit Anonymous Report
- **Method**: `POST`
- **URL**: `/api/reports`
- **Access**: Public (Anonymous)
- **Request Body**:
```json
{
  "category": "SECURITY",
  "description": "There is unauthorized access to a restricted system.",
  "evidenceUrl": "https://example.com/evidence"
}
```
*Note: `evidenceUrl` is optional.*

- **Response (`201 Created`)**:
```json
{
  "message": "Report submitted successfully.",
  "caseCode": "WD-X7K9P2M4Q8T6",
  "status": "SUBMITTED"
}
```

- **Error Codes**:
  - `400 Bad Request`: Validation failure (empty description, invalid category enum, or invalid URL format).

---

#### 2. Track Report by Case Code
- **Method**: `GET`
- **URL**: `/api/reports/{caseCode}`
- **Access**: Public (Anonymous)
- **Example URL**: `/api/reports/WD-X7K9P2M4Q8T6`

- **Response (`200 OK`)**:
```json
{
  "caseCode": "WD-X7K9P2M4Q8T6",
  "category": "SECURITY",
  "status": "UNDER_REVIEW",
  "createdAt": "2026-10-05T10:00:00",
  "updates": [
    {
      "status": "SUBMITTED",
      "message": "Report received.",
      "createdAt": "2026-10-05T10:00:00"
    },
    {
      "status": "UNDER_REVIEW",
      "message": "The report is currently being investigated.",
      "createdAt": "2026-10-05T11:00:00"
    }
  ]
}
```

- **Error Codes**:
  - `404 Not Found`:
```json
{
  "message": "Report not found for the provided case code."
}
```

---

### Moderator Authentication

#### 3. Moderator Login
- **Method**: `POST`
- **URL**: `/api/auth/login`
- **Access**: Public
- **Request Body**:
```json
{
  "username": "moderator",
  "password": "password"
}
```

- **Response (`200 OK`)**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

- **Error Codes**:
  - `401 Unauthorized`: Invalid credentials.
```json
{
  "message": "Invalid username or password."
}
```

---

### Moderator Endpoints (Protected by JWT)

All `/api/moderator/**` endpoints require the header:
`Authorization: Bearer <token>`

Unauthenticated requests receive `401 Unauthorized`:
```json
{
  "message": "Unauthorized"
}
```

---

#### 4. List and Filter Reports
- **Method**: `GET`
- **URL**: `/api/moderator/reports`
- **Optional Query Parameters**:
  - `status`: Filter by status (`SUBMITTED`, `UNDER_REVIEW`, `RESOLVED`, `DISMISSED`)
  - `category`: Filter by category (`SECURITY`, `HARASSMENT`, `CORRUPTION`, `TECHNICAL`, `OTHER`)
- **Examples**:
  - `GET /api/moderator/reports?status=UNDER_REVIEW`
  - `GET /api/moderator/reports?category=SECURITY`
  - `GET /api/moderator/reports?status=SUBMITTED&category=TECHNICAL`

- **Response (`200 OK`)**:
```json
[
  {
    "id": "e2f1a634-9988-4687-84f9-2b02a8fcda6e",
    "category": "SECURITY",
    "description": "Unauthorized access to internal admin dashboard.",
    "evidenceUrl": "https://example.com/evidence",
    "status": "UNDER_REVIEW",
    "createdAt": "2026-10-05T10:00:00",
    "updatedAt": "2026-10-05T11:00:00"
  }
]
```

---

#### 5. Get Individual Report Details
- **Method**: `GET`
- **URL**: `/api/moderator/reports/{id}`
- **Access**: Moderator only

- **Response (`200 OK`)**:
```json
{
  "id": "e2f1a634-9988-4687-84f9-2b02a8fcda6e",
  "category": "SECURITY",
  "description": "Unauthorized access to internal admin dashboard.",
  "evidenceUrl": "https://example.com/evidence",
  "status": "UNDER_REVIEW",
  "createdAt": "2026-10-05T10:00:00",
  "updatedAt": "2026-10-05T11:00:00",
  "updates": [
    {
      "status": "SUBMITTED",
      "message": "Report received.",
      "createdAt": "2026-10-05T10:00:00"
    },
    {
      "status": "UNDER_REVIEW",
      "message": "The report is currently being investigated.",
      "createdAt": "2026-10-05T11:00:00"
    }
  ]
}
```

- **Error Codes**:
  - `404 Not Found`: Report ID does not exist.

---

#### 6. Update Report Status
- **Method**: `PATCH`
- **URL**: `/api/moderator/reports/{id}/status`
- **Access**: Moderator only
- **Request Body**:
```json
{
  "status": "RESOLVED",
  "message": "Security patch deployed and access restored to normal."
}
```

- **Response (`200 OK`)**:
Returns the updated report object including the newly appended update in `updates`.

- **Error Codes**:
  - `400 Bad Request`: Invalid status transition (e.g. attempting `SUBMITTED` &rarr; `RESOLVED`).
```json
{
  "message": "Invalid status transition from SUBMITTED to RESOLVED."
}
```

---

## 6. Privacy & Security Design

Privacy is the central architectural requirement of WhistleDrop:

1. **Zero Identity Storage**:
   The `reports` table contains no columns for name, email, phone number, student ID, IP address, user agent, or device identifiers. It is technically impossible to leak reporter identity from the database because it is never saved.

2. **Hashed Case Codes**:
   When a report is created, a high-entropy case code is generated using `java.security.SecureRandom`. Only the SHA-256 hash of this case code is stored in the database:
   ```text
   Plaintext Case Code: WD-X7K9P2M4Q8T6 (Returned ONLY once upon submission)
   Stored in DB:        9f83... (SHA-256 hash)
   ```
   When tracking a report, the user supplies their case code. The backend hashes it and queries `WHERE case_code_hash = :hash`. Even an attacker with complete read access to the database cannot determine the original case codes.

3. **Information Concealment**:
   Anonymous tracking endpoints return only the necessary public tracking fields (`caseCode`, `category`, `status`, `createdAt`, `updates`). Internal database primary keys (UUIDs) are omitted from public responses.

4. **BCrypt & JWT for Moderators**:
   Moderator passwords are encrypted using BCrypt with salting. All moderator APIs require a valid Bearer JWT.

---

## 7. Status Workflow

The system enforces a strict finite state machine for report lifecycles:

```text
       SUBMITTED
           │
           ▼
     UNDER_REVIEW
      ├───┬───┤
      │   │   │
      ▼   │   ▼
RESOLVED  │  DISMISSED
```

### Transition Rules:
- **Allowed**:
  - `SUBMITTED` &rarr; `UNDER_REVIEW`
  - `UNDER_REVIEW` &rarr; `RESOLVED`
  - `UNDER_REVIEW` &rarr; `DISMISSED`
- **Disallowed**:
  - `SUBMITTED` &rarr; `RESOLVED` (cannot resolve without review)
  - `SUBMITTED` &rarr; `DISMISSED` (cannot dismiss without review)
  - `RESOLVED` &rarr; `UNDER_REVIEW` (final state)
  - `DISMISSED` &rarr; `RESOLVED` (final state)
  - Any transition to the same status

Attempting an invalid transition immediately returns `400 Bad Request` with an explanatory message.

---

## 8. Design Decisions

- **Simple 3-Tier Layering**: `Controller` &rarr; `Service` &rarr; `Repository` structure with standard Java classes and straightforward methods that any developer can understand and explain in a viva.
- **Standard Plain POJOs**: Entities and DTOs use explicit, standard Java constructors, getters, and setters to eliminate compilation ambiguities and complex Lombok processing.
- **Unified Global Exception Handling**: Handled via `@RestControllerAdvice` in `GlobalExceptionHandler`. All errors return a predictable `{"message": "..."}` format.
- **Embedded H2 for Automated Tests**: While production defaults to PostgreSQL, automated tests use an in-memory H2 database configured in PostgreSQL compatibility mode so tests run cleanly in any environment without external dependencies.

---

## 9. Automated Testing

The automated test suite in `WhistleDropApplicationTests` verifies the key behaviors:

1. **Report Creation**: Verifies anonymous submission produces a `201 Created`, valid `WD-` case code, and `SUBMITTED` status.
2. **Invalid Category**: Verifies unknown enum values return `400 Bad Request`.
3. **Empty Description**: Verifies Bean Validation catches empty input with `400 Bad Request`.
4. **Case-Code Tracking**: Verifies anonymous users can track their reports and that internal database UUIDs are concealed.
5. **Invalid Case Code**: Verifies non-existent case codes return `404 Not Found`.
6. **Moderator Login**: Verifies credentials return a JWT token and bad credentials return `401 Unauthorized`.
7. **Unauthorized Moderator Endpoint**: Verifies protected `/api/moderator/**` endpoints reject unauthenticated access with `401 Unauthorized`.
8. **Valid Status Transition**: Verifies `SUBMITTED` &rarr; `UNDER_REVIEW` &rarr; `RESOLVED` creates corresponding update records.
9. **Invalid Status Transition**: Verifies skipping `UNDER_REVIEW` directly to `RESOLVED` is rejected with `400 Bad Request`.

### Running Tests
Execute:

```bash
mvn test
```
