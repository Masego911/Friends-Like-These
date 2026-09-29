# Friends Like These

### Games Night Management & Live Scoring Platform

Friends Like These is a full-stack web application for managing live team-based games nights from registration through to live scoring, round management and historical results.

The platform brings together team registration, game setup, round management, live scoring, leaderboards and event history in one system.

Organisers manage the game through a secure administration dashboard while participants and spectators follow the competition through audience-facing scoreboards, including a dedicated Smart TV application.

Friends Like These started as a practical solution for running a CampusKey games night and has since evolved into a reusable full-stack event platform.

---

## Live Applications

### Main Application

https://friendsliketheseck.netlify.app

Used for the main Friends Like These experience and administration.

### Dedicated Smart TV Scoreboard

https://friendslikethesetv.netlify.app

A separate audience-facing application optimised for displaying the live competition on TVs and large event screens.

### Production Backend

Hosted on Microsoft Azure App Service.

The backend provides the REST API used by both frontend applications and connects to Azure SQL for persistent game data.

---

# Project Evolution

## The Original Friends Like These

Friends Like These was originally developed for a live CampusKey student games night.

The first version focused on solving the immediate operational challenge of running the event.

Instead of manually tracking registrations and scores, the application provided a digital scoreboard that organisers could update while participants followed the leaderboard.

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

However, it was primarily a frontend-driven event tool rather than a complete games-management system.

---

# The Rebuild

The current version is a substantial rebuild of the original application.

Instead of developing Friends Like These around one specific event, the application has been redesigned as a reusable **Games Night Management Platform**.

The rebuild introduces a full-stack architecture:

```text
                    Google Forms
                         |
                         v
                    Google Sheets
                         |
                         v
                 Google Sheets API
                         |
                         v
                Java Spring Boot
                         |
                         v
                     Azure SQL
                         |
                 REST API / HTTPS
                         |
              +----------+----------+
              |                     |
              v                     v
     Main React Application   Smart TV Application
```

Google Forms and Google Sheets remain part of the registration workflow, but the Spring Boot backend controls application state, business rules, scoring, events, rounds, authentication and persistence.

This allows Friends Like These to manage the complete lifecycle of a games night rather than only displaying scores.

---

# What Friends Like These Does

A games night follows a structured process:

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
Google Sheets Sync
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
Round Result
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

The administration dashboard and audience displays use the same backend game state but serve different purposes.

The **Admin Dashboard** is designed for the organiser running the event.

The **Public Scoreboard** is designed for participants and spectators.

The **Smart TV Scoreboard** is a dedicated lightweight display for TVs and large event screens.

---

# Features

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

This separates operational controls from the audience-facing competition screens.

---

# Live Scoring

The Live Scoring interface allows organisers to update team scores while the competition is running.

Quick scoring controls include:

```text
-10   -5   -1   +1   +5   +10
```

Custom score adjustments can also be made when required.

Teams are ranked according to their current scores.

Every score adjustment is processed by the backend rather than treating browser state as the permanent source of truth.

Score changes are also recorded as score activity.

The general scoring flow is:

```text
Admin changes score
        |
        v
React Admin
        |
        v
Spring Boot API
        |
        v
Azure SQL
        |
        v
Updated leaderboard
        |
        +------------------+
        |                  |
        v                  v
Main Scoreboard      Smart TV Scoreboard
```

---

# Round Management

Games can contain multiple rounds.

Each round has its own lifecycle.

```text
NOT_STARTED
     |
     v
IN_PROGRESS
     |
     v
COMPLETED
```

Administrators can start and end rounds from the game controls.

Scoring is only permitted when the game and round are in the appropriate state.

The backend enforces these rules rather than relying only on disabled frontend controls.

---

# Live Audience Experience

The audience scoreboard responds to the state of the game.

## Between Rounds

The audience sees the cumulative:

```text
OVERALL STANDINGS
```

## When a Round Starts

Starting a round changes the audience experience to:

```text
GET READY

ROUND N

ROUND IN PROGRESS
```

The scoreboard can then display standings for that specific round.

## During a Round

The live scoreboard retrieves updated scores from the backend so changes made through the administration interface appear on the audience display without requiring manual refreshes.

The main scoreboard experience includes live visual behaviour such as score and leaderboard updates.

## When a Round Ends

The audience receives a round-result presentation before returning to the updated cumulative standings.

This keeps the scoreboard aligned with the actual game lifecycle controlled by the organiser.

---

# Dedicated Smart TV Scoreboard

Friends Like These includes a separate frontend specifically for Smart TVs and large event displays.

Production TV application:

https://friendslikethesetv.netlify.app

The TV application is intentionally separated from the administration interface.

This means an organiser can operate the game from a laptop, desktop or suitable mobile device while the audience sees only the scoreboard.

```text
ORGANISER

Admin Laptop / Device
        |
        v
Azure API
        |
        v
Azure SQL
        |
        v
Smart TV
        |
        v
AUDIENCE
```

## TV Features

The dedicated TV application provides:

- Friends Like These branding
- Live leaderboard
- Team rankings
- Team scores
- Overall standings
- Round-specific standings
- Current round information
- Round-start presentation
- Round-winner presentation
- Registration countdown
- Registration QR code
- Live connection status
- Automatic score refresh
- Reconnection state

The TV application contains no administrative controls.

---

# Smart TV Compatibility

Smart TVs can use browser engines that are considerably older than modern desktop browsers.

For that reason, the TV application is maintained as an independent Vite application.

Its production build uses:

```text
@vitejs/plugin-legacy
```

to generate legacy JavaScript bundles and browser polyfills in addition to the normal modern production bundle.

The production build therefore contains both modern and legacy-compatible assets.

The TV application also uses a Netlify API proxy:

```text
TV Browser
     |
     v
Netlify /api/*
     |
     v
Azure Spring Boot API
```

This allows the TV application to make same-origin `/api` requests while Netlify forwards those requests to the production backend.

The architecture also allows TV compatibility improvements to be made without risking the administration application.

---

# Registration Countdown & QR Code

While registration is open, the Smart TV scoreboard can display a live countdown to the registration deadline.

The display also contains a QR code linking participants directly to the Friends Like These registration form.

This means participants at the venue can scan the event screen using their phones and register their team.

```text
Smart TV
   |
   v
QR Code
   |
   v
Google Form
   |
   v
Google Sheets
   |
   v
Friends Like These
```

---

# Team Management

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

Responses are stored in a private Google Sheet and synchronised with the Friends Like These backend.

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
Azure SQL
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
- Protection against simultaneous synchronisation operations

Google Sheets acts as the registration source rather than as the application's primary database.

The production application securely supplies Google API credentials through server-side environment configuration rather than exposing credentials to the browser or repository.

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

Historical data is presented separately from the current event so previous results do not interfere with an active games night.

---

# Authentication & Security

Administrative functionality is protected through Spring Security.

The application includes:

- Administrator authentication
- Protected administrative endpoints
- Password hashing
- Login throttling
- Session-based authentication
- Role-based backend security
- CSRF protection
- Production cookie configuration
- CORS configuration
- Public read-only scoreboard endpoints

Audience-facing leaderboard requests do not require administrator authentication.

Operations that modify scores, teams, registration state or game state remain protected.

Sensitive credentials are not stored directly in the repository.

---

# Score Integrity & Concurrency

Friends Like These is designed for a live environment where several operations can occur almost simultaneously.

The backend therefore includes concurrency protection for critical operations.

Score changes update the relevant score and create associated score activity transactionally.

Database locking is used during important scoring operations to reduce the possibility of conflicting updates.

Event lifecycle mutations also use locking and database constraints to protect current game state.

Registration synchronisation uses a bounded background executor and prevents multiple synchronisation jobs from processing the same registration source simultaneously.

---

# Production Architecture

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
                +---------------------------+
                |    SPRING BOOT BACKEND    |
                |---------------------------|
                | Authentication            |
                | Event Management          |
                | Registration              |
                | Team Management           |
                | Round Engine              |
                | Scoring                   |
                | Score Activity            |
                | Previous Games            |
                +-------------+-------------+
                              |
                              v
                          Azure SQL
                              ^
                              |
                         REST API
                              |
              +---------------+---------------+
              |                               |
              v                               v
       Main React App                  Smart TV App
              |                               |
              v                               v
          Netlify                         Netlify
              |                               |
              v                               v
      Organiser / Users                    Audience
```

The Spring Boot backend is the authoritative source for game state and business rules.

Both frontend applications consume the same backend data.

---

# Technology Stack

| Area | Technology |
|---|---|
| Main Frontend | React |
| TV Frontend | React |
| Frontend Language | TypeScript |
| Build Tool | Vite |
| TV Compatibility | Vite Legacy Plugin |
| Backend | Spring Boot |
| Backend Language | Java 21 |
| Database | Microsoft SQL Server / Azure SQL |
| Persistence | Spring Data JPA / Hibernate |
| Security | Spring Security |
| Registration | Google Forms |
| Registration Integration | Google Sheets API |
| Database Migrations | Flyway |
| Backend Build | Gradle |
| API | REST |
| Frontend Hosting | Netlify |
| Backend Hosting | Microsoft Azure App Service |
| Production Database | Azure SQL |
| Version Control | Git & GitHub |

---

# Repository Structure

Friends Like These is maintained as one full-stack project.

```text
Friends-Like-These/
|
|-- src/                         # Main React application
|   |-- admin/
|   |-- api/
|   |-- components/
|   |-- models/
|   `-- ...
|
|-- public/
|
|-- tv/                          # Dedicated Smart TV application
|   |-- src/
|   |   |-- App.tsx
|   |   |-- Countdown.tsx
|   |   |-- RegistrationQR.tsx
|   |   `-- ...
|   |
|   |-- public/
|   |-- package.json
|   |-- vite.config.ts
|   `-- netlify.toml
|
|-- backend/                     # Spring Boot backend
|   |-- src/
|   |   |-- main/
|   |   |   |-- java/
|   |   |   `-- resources/
|   |   `-- test/
|   |
|   |-- gradle/
|   |-- build.gradle
|   |-- gradlew
|   `-- gradlew.bat
|
|-- package.json
|-- vite.config.ts
`-- README.md
```

The dedicated TV application can therefore evolve independently while continuing to use the same backend and game data.

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

This separates authentication, events, registration, scoring and team-management responsibilities rather than placing application business logic directly inside controllers.

---

# Data Flow

A central design principle of the rebuild is that the browser is not the permanent source of truth.

For scoring:

```text
User Action
     |
     v
React
     |
     v
REST API
     |
     v
Spring Boot
     |
     v
Azure SQL
     |
     v
API Response
     |
     v
Updated Interface
```

For registration:

```text
Participant
     |
     v
Google Form
     |
     v
Google Sheets
     |
     v
Registration Sync
     |
     v
Spring Boot
     |
     v
Azure SQL
     |
     v
Admin / Game
```

This architecture separates presentation from persistent application state.

---

# Running the Main Frontend Locally

## Requirements

Install:

- Node.js
- npm
- Git

From the repository root:

```powershell
npm install
npm run dev
```

The development frontend normally runs on:

```text
http://localhost:5173
```

The backend API can be configured using:

```text
VITE_API_BASE_URL=http://localhost:8080
```

---

# Running the TV Application Locally

From the repository root:

```powershell
cd tv
npm install
npm run dev
```

Create a production TV build using:

```powershell
npm run build
```

The production build generates both modern and legacy browser assets.

---

# Running the Backend

Requirements include:

- Java 21
- Gradle wrapper
- Microsoft SQL Server for local development

From the backend project:

```powershell
.\gradlew.bat bootRun
```

The local backend normally runs on:

```text
http://localhost:8080
```

A configured SQL Server database must be available to the backend.

Google Cloud credentials are additionally required when using the Google Sheets registration integration locally.

---

# Database Migrations

Production database schema management uses Flyway.

Database migrations are stored under the backend resources and applied by the production application.

Production uses schema validation rather than relying on Hibernate to silently generate or modify the database structure.

This makes database changes explicit and reproducible.

---

# Testing

The backend includes automated tests covering important application behaviour.

Test coverage includes areas such as:

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

Run the backend test suite with:

```powershell
.\gradlew.bat test
```

The frontend production build can be validated using:

```powershell
npm run build
```

The dedicated TV production build can be validated from `/tv` using:

```powershell
npm run build
```

---

# Deployment

Friends Like These currently uses separate deployments for its major components.

## Main Frontend

Netlify:

https://friendsliketheseck.netlify.app

## Smart TV Frontend

Netlify:

https://friendslikethesetv.netlify.app

## Backend

Microsoft Azure App Service.

## Database

Azure SQL Database in South Africa North.

This separation allows each presentation layer to be optimised for its purpose while maintaining a single authoritative backend.

---

# Current Development Status

Friends Like These has progressed beyond the original frontend-only scoreboard.

Implemented areas now include:

- Full-stack React and Spring Boot architecture
- SQL persistence
- Azure SQL production database
- Azure-hosted backend
- Netlify frontend deployment
- Administrator authentication
- Protected administrative endpoints
- Event lifecycle management
- Round management
- Live scoring
- Score activity
- Team management
- Google Forms registration workflow
- Google Sheets registration integration
- Registration synchronisation architecture
- Previous Games
- Public leaderboard endpoints
- Round-aware audience experience
- Dedicated Smart TV application
- Registration QR code
- Registration countdown
- Smart TV legacy-browser build
- Separate Smart TV production deployment
- Database migrations with Flyway
- Concurrency protection for critical operations
- Automated backend testing

---

# Roadmap

The next development areas include:

- Continued Smart TV compatibility testing
- Additional automated frontend testing
- GitHub Actions continuous integration
- Continuous deployment
- Production monitoring and health checks
- Improved operational logging
- Event analytics dashboard
- Game and participation analytics
- Continued accessibility improvements

---

# Why Friends Like These Exists

Friends Like These started with a real operational problem.

Running a live games night means managing registrations, participants, teams, rounds and scores while simultaneously keeping the audience informed about what is happening.

The original application solved the immediate scoring and leaderboard problem.

The rebuild takes that experience further by treating the games night as a complete event lifecycle — from registration to live competition and historical results.

The project has also become an opportunity to apply software-engineering concepts to a system with a real use case, including:

- Full-stack application architecture
- REST API design
- Relational database design
- Authentication and security
- External API integration
- Transaction management
- Concurrency
- Database migrations
- Cloud deployment
- Automated testing
- Responsive interface design
- Legacy-browser compatibility
- Separation of administrative and audience experiences

---

# Project Status

**Active development**

The current full-stack rebuild is maintained on:

```text
react-rebuild
```

The project now has a deployed main application, production backend, Azure SQL database and dedicated Smart TV scoreboard.

Further development is focused on reliability, testing, deployment automation, monitoring and analytics while continuing to improve the live games-night experience.
