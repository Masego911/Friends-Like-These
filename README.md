# Friends Like These Backend

Spring Boot backend for the **Friends Like These Games Night Management & Live Scoring Platform**.

The backend provides the REST API, business rules, persistence, authentication, registration synchronisation, event lifecycle, round management and transactional scoring used by the Friends Like These React application.

---

## Technology Stack

| Component | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot |
| Security | Spring Security |
| Persistence | Spring Data JPA / Hibernate |
| Database | Microsoft SQL Server |
| External Integration | Google Sheets API |
| Build Tool | Gradle |
| API | REST |

---

## Backend Responsibilities

The backend is the authoritative source for Friends Like These game state.

It manages:

- Administrator authentication
- Event creation and lifecycle
- Current-event state
- Registration synchronisation
- Team and team-member management
- Game rounds
- Live scoring
- Score activity
- Leaderboards
- Previous-game data
- Concurrency and transactional integrity

Business rules are enforced by the backend rather than relying on frontend controls.

---

# Package Structure

```text
src/main/java/com/friendslikethese/backend/

├── auth/
├── common/
├── config/
├── event/
├── registration/
├── round/
├── score/
└── team/
```

### `auth`

Administrator authentication and security-related functionality.

Includes administrator accounts, login handling, user details, login throttling and authentication responses.

### `common`

Shared backend infrastructure including API errors, business-rule exceptions, resource-not-found handling and global exception handling.

### `config`

Application configuration including security, concurrency configuration and development/data migration support.

### `event`

Manages games and their lifecycle.

The event lifecycle is:

```text
DRAFT
  ↓
REGISTRATION_OPEN
  ↓
REGISTRATION_CLOSED
  ↓
LIVE
  ↓
COMPLETED
  ↓
ARCHIVED
```

The backend explicitly identifies the current event instead of assuming the newest event is current.

### `registration`

Handles the Google Forms / Google Sheets registration pipeline.

```text
Google Form
     ↓
Google Sheets
     ↓
Google Sheets API
     ↓
Registration Sync
     ↓
Team Import
     ↓
SQL Server
```

The registration subsystem supports scheduled and manual synchronisation, previews, duplicate handling, conflict detection and synchronisation status.

### `round`

Controls individual game rounds and round standings.

Round states are:

```text
PENDING
   ↓
IN_PROGRESS
   ↓
COMPLETED
```

Only valid lifecycle transitions are accepted.

### `score`

Handles score adjustments and score activity.

Score updates are processed transactionally so that the team score and corresponding score activity remain consistent.

### `team`

Handles teams, team members, leaderboard information and imported registration data.

---

# Event Lifecycle

Friends Like These treats each games night as an event.

An event progresses through controlled lifecycle states:

```text
DRAFT
→ REGISTRATION_OPEN
→ REGISTRATION_CLOSED
→ LIVE
→ COMPLETED
→ ARCHIVED
```

These states determine which operations are allowed.

For example:

- Registration requires an event with open registration.
- Starting the game requires registration to have closed.
- Scoring requires a live game.
- Scoring also requires a round currently in progress.
- Completed events can be retained as previous games.
- Archived events remain historical records.

---

# Round Engine

A game can contain multiple rounds.

The backend tracks:

- Total rounds
- Current round
- Round number
- Round status
- Round start
- Round completion
- Round standings

A round progresses through:

```text
PENDING → IN_PROGRESS → COMPLETED
```

Round state is used by the scoring system to determine whether score changes are currently permitted.

---

# Scoring

Score changes are performed through the scoring service rather than directly modifying scores from the frontend.

A scoring operation:

```text
Admin Score Request
        ↓
Validate Event
        ↓
Validate Active Round
        ↓
Lock Required Database State
        ↓
Adjust Team Score
        ↓
Create Score Event
        ↓
Commit Transaction
        ↓
Return Authoritative Result
```

The React application uses the returned backend state instead of treating the browser as the authoritative score source.

---

# Score Activity

Score changes create score-event records.

This provides a history of scoring activity for the current event and supports historical game information.

Score activity is associated with the relevant event and team rather than existing only as temporary frontend state.

---

# Registration Integration

Participant registration is handled through Google Forms and Google Sheets.

The backend connects to the Google Sheets API using service-account authentication.

The registration service:

1. Reads registration rows from the configured spreadsheet.
2. Validates and normalises registration data.
3. Identifies teams.
4. Extracts team members.
5. Detects duplicate/conflicting registrations.
6. Imports valid teams into SQL Server.
7. Records the result of the synchronisation.

Automatic synchronisation is handled by a scheduler.

Manual synchronisation can also be requested from the administration interface.

---

# Registration Concurrency

Registration synchronisation uses single-flight behaviour.

If a synchronisation operation is already running, another synchronisation is prevented from processing the same work concurrently.

Background synchronisation runs through a bounded executor so that background jobs cannot create an unlimited number of threads.

---

# Concurrency & Data Integrity

Because Friends Like These operates during live events, several requests may occur at nearly the same time.

The backend therefore contains concurrency protection around critical state changes.

This includes:

- Pessimistic locking for critical scoring operations
- Transactional score updates
- Transactional score-event creation
- Event lifecycle locking
- Database constraints for current-event integrity
- Registration single-flight protection
- Bounded background execution

Optimistic versioning provides an additional safeguard where applicable.

---

# Authentication & Security

Administrative API functionality is protected using Spring Security.

The backend includes:

- Administrator accounts
- Password hashing
- Login handling
- Login throttling
- Protected administrative endpoints
- Session-based authentication

Credentials and private keys must never be committed to the repository.

---

# Configuration

Application configuration is located under:

```text
src/main/resources/application.properties
```

Sensitive values should be supplied through environment variables or local runtime configuration.

Examples include:

```text
GOOGLE_APPLICATION_CREDENTIALS
ADMIN_USERNAME
ADMIN_PASSWORD
```

Google service-account JSON files, private keys, passwords and local environment files must remain outside Git.

---

# Database

The backend currently uses Microsoft SQL Server.

The database stores application state including:

- Events
- Teams
- Team members
- Scores
- Score activity
- Rounds
- Registration information
- Administrator accounts

The database is the persistent source of truth for the backend.

---

# Running the Backend

## Requirements

Install:

- Java 21
- Microsoft SQL Server
- Git

The project includes the Gradle Wrapper, so a separate Gradle installation is not required.

From the `backend` directory:

```powershell
.\gradlew.bat bootRun
```

The API normally runs on:

```text
http://localhost:8080
```

The SQL Server connection and required environment variables must be configured before starting the application.

---

# Running Tests

From the `backend` directory:

```powershell
.\gradlew.bat test
```

The test suite includes coverage for:

- Application startup
- Security
- Authentication
- Global exception handling
- Event lifecycle
- Event rounds
- Concurrent operations
- Executor configuration
- Google Sheets transport
- Google Sheets parsing
- Registration controllers
- Registration imports
- Registration synchronisation
- Registration scheduling
- Round engine behaviour
- Score adjustment regression behaviour
- Score activity reset

---

# Test Structure

```text
src/test/java/com/friendslikethese/backend/

├── auth/
├── common/
├── concurrency/
├── config/
├── event/
├── registration/
├── round/
└── score/
```

The backend uses both focused tests and integration tests to verify business rules across application layers.

---

# Frontend Integration

The backend serves the React application through REST endpoints.

During local development:

```text
React
http://localhost:5173

        ↓ REST

Spring Boot
http://localhost:8080

        ↓

SQL Server
```

The frontend API location is configured through:

```text
VITE_API_BASE_URL=http://localhost:8080
```

---

# Current Development

The backend currently supports the core games-night lifecycle required by the rebuilt Friends Like These application.

Further backend work will support the continuing development of:

- Public round-specific leaderboard behaviour
- Production database migration management
- CI/CD
- Production deployment
- Monitoring and health checks
- Analytics

---

# Main Project

This backend is part of the **Friends Like These** full-stack repository.

The React frontend is located at the repository root while this Spring Boot application is maintained under:

```text
/backend
```

See the root `README.md` for the complete Friends Like These project overview.
