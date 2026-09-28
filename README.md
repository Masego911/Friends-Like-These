# Friends Like These

### Games Night Management & Live Scoring Platform

Friends Like These is a full-stack web application for managing live team-based games nights.

The platform brings together team registration, game setup, round management, live scoring, leaderboards and event history in one system. Organisers manage the game through a secure administration dashboard while participants and spectators follow the competition through a separate public scoreboard.

Friends Like These started as a practical solution for running a CampusKey games night and has since evolved into a reusable full-stack event platform.

---

## Project Evolution

### The Original Friends Like These

Friends Like These was originally developed for a live CampusKey student games night.

The first version focused on solving the immediate operational challenge of running the event. Instead of manually tracking registrations and scores, the application provided a digital scoreboard that organisers could update while participants followed the leaderboard.

The original system included:

- Team registration
- QR-based access to registration
- Google Forms and Google Sheets integration
- Live team scoring
- Automatic leaderboard ranking
- Administrator scoring controls
- Registration countdown
- Firebase Realtime Database synchronisation
- Responsive display for event screens and mobile devices

That version demonstrated the value of the idea during an actual live event.

However, it was primarily a frontend-driven event tool rather than a complete games management system.

---

## The Rebuild

The current version is a substantial rebuild of the original application.

Instead of developing Friends Like These around one specific event, the application is being redesigned as a reusable **Games Night Management Platform**.

The rebuild introduces a proper full-stack architecture:

```text
React + TypeScript
        |
        | REST API
        v
Java Spring Boot
        |
        v
Microsoft SQL Server
```

Google Forms and Google Sheets remain part of the registration workflow, but the Spring Boot backend now controls application state, business rules, scoring, events, rounds and persistence.

This allows Friends Like These to manage the full lifecycle of a games night rather than only displaying scores.

---

# What Friends Like These Does

A games night can now move through a structured process:

```text
Create Game
     |
     v
Open Registration
     |
     v
Teams Register
     |
     v
Close Registration
     |
     v
Start Game
     |
     v
Start Round
     |
     v
Live Scoring
     |
     v
End Round
     |
     v
Next Round
     |
     v
Complete Game
     |
     v
Previous Games
```

The public scoreboard and administration dashboard use the same backend game state but serve different purposes.

The **Admin Dashboard** is designed for the organiser running the event.

The **Public Scoreboard** is designed for the audience watching the game.

---

# Features

## Public Scoreboard

The public scoreboard provides the audience-facing view of the competition.

It displays:

- Competing teams
- Current scores
- Leaderboard positions
- Current round information
- Registration information
- Game status

The scoreboard automatically retrieves current game information from the backend.

It is designed to work across large displays, laptops, tablets and mobile devices.

---

## Admin Dashboard

Organisers manage Friends Like These through an authenticated administration interface.

The dashboard includes:

- Dashboard overview
- Live Scoring
- Team Management
- Registration Management
- Game Setup
- Previous Games
- Settings

This separates operational controls from the public competition screen.

---

## Live Scoring

The Live Scoring interface allows organisers to update team scores while the competition is running.

Quick scoring controls include:

```text
-10   -5   -1   +1   +5   +10
```

Custom score adjustments can also be made when required.

Teams are automatically ranked according to their current scores.

Every score adjustment is processed by the backend and recorded as score activity.

---

## Round Management

Games can contain multiple rounds.

Each round has its own lifecycle:

```text
PENDING
   |
   v
IN_PROGRESS
   |
   v
COMPLETED
```

Administrators can start and end rounds directly from the game controls.

Scoring is only permitted when:

```text
Game Status = LIVE
AND
Round Status = IN_PROGRESS
```

These rules are enforced by the backend rather than relying only on disabled frontend controls.

---

## Game Progress Controls

Round controls are also available directly from the Live Scoring interface.

This allows the organiser running the game to:

- Start the next round
- Score teams
- End the current round
- Continue to the next round

without repeatedly moving between the scoring screen and Game Setup.

---

## Team Management

The system supports management of teams participating in the current game.

Administrators can:

- Create teams
- View team members
- Edit teams
- Manage imported teams
- View current scores
- View leaderboard positions

Team information is persisted in SQL Server.

---

# Registration Management

Friends Like These integrates with Google Forms and Google Sheets for participant registration.

Participants register through a Google Form.

Their responses are stored in a private Google Sheet and synchronised with the Friends Like These backend.

```text
Google Form
     |
     v
Google Sheets
     |
     v
Google Sheets API
     |
     v
Spring Boot
     |
     v
SQL Server
     |
     v
React
```

The registration system supports:

- Automatic registration synchronisation
- Manual synchronisation
- Registration previews
- Duplicate-team detection
- Conflict detection
- Team-member importing
- Registration status monitoring
- Protection against simultaneous sync operations

Google Sheets is used as the registration source rather than as the application's primary database.

---

# Event Lifecycle

Games follow an explicit lifecycle:

```text
DRAFT
  |
  v
REGISTRATION_OPEN
  |
  v
REGISTRATION_CLOSED
  |
  v
LIVE
  |
  v
COMPLETED
  |
  v
ARCHIVED
```

This lifecycle controls what the system allows at each stage of an event.

For example, teams cannot continue registering after registration closes, and scoring cannot occur before the game and a round have started.

The backend maintains an explicitly designated current event instead of assuming that the most recently created event is the active game.

---

# Previous Games

Completed games are retained rather than being deleted when a new event begins.

The Previous Games area provides access to historical information including:

- Event details
- Participating teams
- Team members
- Final leaderboard
- Rounds
- Score activity

This allows Friends Like These to build an event history over time.

---

# Authentication & Security

Administrative functionality is protected through the Spring Boot security layer.

The application includes:

- Administrator authentication
- Protected admin endpoints
- Password hashing
- Login throttling
- Session-based authentication
- Role-based backend security

Sensitive credentials are not stored directly in the repository.

---

# Score Integrity & Concurrency

Friends Like These is designed for a live environment where several operations can happen at almost the same time.

The backend therefore includes concurrency protection for critical operations.

Score changes update the team score and create the associated score activity within the same transactional operation.

Database locking is used when updating scores to reduce the possibility of conflicting updates.

Event lifecycle changes also use locking and database constraints to protect the current game state.

Registration synchronisation uses a bounded background executor and prevents multiple synchronisation jobs from processing the same registration source simultaneously.

---

# Architecture

```text
                     PARTICIPANTS
                           |
                           v
                     Google Form
                           |
                           v
                     Google Sheets
                           |
                           v
                   Google Sheets API
                           |
                           v
              +--------------------------+
              |   SPRING BOOT BACKEND    |
              |--------------------------|
              | Authentication           |
              | Event Management         |
              | Registration             |
              | Team Management          |
              | Round Engine             |
              | Scoring                  |
              | Score Activity           |
              | Previous Games           |
              +------------+-------------+
                           |
                           v
                      SQL Server
                           ^
                           |
                        REST API
                           |
                 +---------+---------+
                 |                   |
                 v                   v
          Admin Dashboard      Public Scoreboard
                 \                   /
                  \                 /
                   +--- React -----+
```

The Spring Boot backend is the authoritative source for game state and business rules.

The React frontend consumes the backend REST API and presents that state through the admin and public interfaces.

---

# Technology Stack

| Area | Technology |
|---|---|
| Frontend | React |
| Frontend Language | TypeScript |
| Build Tool | Vite |
| Backend | Spring Boot |
| Backend Language | Java 21 |
| Database | Microsoft SQL Server |
| Persistence | Spring Data JPA / Hibernate |
| Security | Spring Security |
| Registration Integration | Google Sheets API |
| Backend Build | Gradle |
| API | REST |
| Version Control | Git & GitHub |

---

# Repository Structure

Friends Like These is maintained as **one full-stack repository**.

```text
Friends-Like-These/
│
├── src/                     # React frontend
├── public/                  # Frontend assets
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   │
│   ├── gradle/
│   ├── build.gradle
│   ├── gradlew
│   └── gradlew.bat
│
├── package.json
├── vite.config.ts
└── README.md
```

The frontend remains at the repository root while the Spring Boot application is contained in `/backend`.

---

# Backend Organisation

The backend is organised around the application's main business domains:

```text
auth/
common/
config/
event/
registration/
round/
score/
team/
```

This separates authentication, events, registration, scoring and team-management responsibilities instead of placing the application's business logic directly inside controllers.

---

# Running the Project Locally

## Requirements

You will need:

- Node.js
- npm
- Java 21
- Microsoft SQL Server
- Git

Google Cloud credentials are additionally required when using the live Google Sheets registration integration.

---

## Run the Frontend

From the repository root:

```powershell
npm install
npm run dev
```

The development frontend runs on:

```text
http://localhost:5173
```

The backend API location can be configured using:

```text
VITE_API_BASE_URL=http://localhost:8080
```

---

## Run the Backend

From the repository root:

```powershell
cd backend
.\gradlew.bat bootRun
```

The backend runs on:

```text
http://localhost:8080
```

A configured SQL Server database must be available to the backend.

---

# Testing

The backend includes automated tests covering the application's important business behaviour.

Current test coverage includes:

- Application startup
- Authentication and security
- Global exception handling
- Concurrent operations
- Event lifecycle
- Event and round integration
- Google Sheets communication
- Registration importing
- Registration synchronisation
- Round engine behaviour
- Score adjustments
- Score activity reset

Run the backend test suite from `/backend`:

```powershell
.\gradlew.bat test
```

The frontend production build can be validated using:

```powershell
npm run build
```

---

# Currently in Development

The next major development area is the public live-scoreboard experience.

During an active round, the public scoreboard is planned to automatically transition from the overall leaderboard to the standings for that specific round.

When the administrator ends the round, the audience display will transition back to the updated overall standings.

Planned improvements include:

- Round-specific live standings
- Automatic Overall → Round transitions
- Round Complete transitions
- Animated score changes
- Smooth leaderboard position changes
- Branded transition effects
- Reduced-motion accessibility support

These features are part of the current development plan and are not yet documented as completed functionality.

---

# Roadmap

The broader development roadmap includes:

- Public scoreboard round experience
- Expanded automated testing
- GitHub Actions continuous integration
- Database migration management
- Production backend deployment
- Azure SQL
- Continuous deployment
- Monitoring and health checks
- Event analytics dashboard

---

# Why Friends Like These Exists

Friends Like These started with a real operational problem.

Running a live games night means managing registrations, participants, teams, rounds and scores while simultaneously keeping the audience informed about what is happening.

The original application solved the immediate scoring and leaderboard problem.

The rebuild takes that experience further by treating the games night as a complete event lifecycle — from registration to live competition and finally historical results.

The project has also become an opportunity to apply software-engineering concepts to a system that has a real use case, including:

- Full-stack application architecture
- REST API design
- Relational database design
- Authentication and security
- External API integration
- Transaction management
- Concurrency
- Automated testing
- Responsive interface design

---

## Project Status

**Active development**

The current full-stack rebuild is maintained on:

```text
react-rebuild
```

The rebuilt application is being developed and tested progressively before replacing the earlier production version.
