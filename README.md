# Friends Like These

Friends Like These is a responsive games-night scoring and event-management application designed for live team competitions.

The project began as a CampusKey games-night application and is now being rebuilt as a more structured full-stack system using React, TypeScript and a planned Java Spring Boot backend.

The application is designed to support live events where administrators manage teams and scores while participants and spectators follow a public leaderboard in real time.

The interface is being designed for:

- TVs and large event displays
- Desktop and laptop computers
- Tablets
- Mobile phones

---

# Project Status

The project is currently undergoing a full rebuild.

The active frontend rebuild is located on the:

`react-rebuild`

branch.

The original Friends Like These implementation remains preserved on:

`main`

Current development status:

- React frontend: In development
- TypeScript migration: Implemented
- Responsive scoreboard: Implemented
- Admin dashboard: Implemented
- Team management: Implemented locally
- Score management: Implemented locally
- Score history: Implemented locally
- Spring Boot backend: Next development phase
- PostgreSQL persistence: Planned
- Authentication and authorisation: Planned
- Real-time backend synchronisation: Planned
- Azure deployment: Planned

---

# Project Background

The original Friends Like These application was developed to support a CampusKey student games-night competition.

The first implementation demonstrated the core concept by allowing event organisers to manage teams, update scores and display a live leaderboard.

The rebuild expands that concept into a maintainable full-stack application.

The new version separates:

- presentation
- application state
- business logic
- persistence
- authentication
- external integrations

This allows the application to grow beyond a single event while remaining easier to maintain and test.

---

# Current Features

## Public Scoreboard

The public scoreboard provides the spectator-facing event experience.

Current functionality includes:

- Team leaderboard
- Automatic ranking based on score
- Team names and members
- Gold, silver and bronze medal indicators
- Registration countdown
- Registration QR code
- Responsive team cards
- Automatic score display updates when application state changes
- Layouts designed for TVs, desktops, tablets and mobile phones

The scoreboard is intentionally separated from administrative controls so that it can be displayed on a public event screen.

---

# Admin Dashboard

The administration interface provides controls for managing the live event.

Current functionality includes:

- Event settings
- Registration deadline management
- Team management
- Add Team
- Edit Team
- Delete Team
- Delete confirmation
- Undo accidental team deletion
- Quick score adjustments
- Custom score adjustments
- Score history
- Reset All Scores
- Reset confirmation
- Responsive administration layout

---

# Team Management

Administrators can currently:

- Add teams
- Edit existing teams
- Delete teams
- View team member counts
- Manage team scores

Deleting a team requires confirmation.

After deletion, an undo notification remains available for six seconds so that an accidental deletion can be reversed.

---

# Score Management

Administrators can award or deduct points directly from the team-management interface.

Quick score controls currently include:

- +5
- +10
- +15
- +20
- -5
- Custom

Each quick-score button uses a distinct visual treatment so administrators can identify scoring actions quickly during a live event.

Custom score adjustments allow an administrator to specify an amount and reason.

Team scores are prevented from becoming negative.

---

# Score History

Score changes are recorded in the frontend application state.

A score event contains information such as:

- Team ID
- Amount changed
- Reason
- Timestamp

The Score History panel provides administrators with a record of scoring activity during the event.

The panel uses a fixed-size scrollable interface so that a large score history does not continually increase the height of the dashboard.

Persistent score history will be implemented when the Spring Boot backend and PostgreSQL database are introduced.

---

# Reset All Scores

Administrators can reset all team scores to zero.

Because this is a high-impact operation, the application requires confirmation before performing the reset.

The backend version will also persist reset activity so that important scoring operations remain auditable.

---

# Responsive Design

Responsive design is a core project requirement.

Friends Like These must operate correctly on multiple device types.

## TV / Large Display

The public scoreboard should prioritise:

- Large team names
- Large scores
- Rankings
- Registration countdown
- High visibility
- Readability from a distance

Administrative controls are not the primary TV use case.

## Desktop / Laptop

Desktop devices provide the complete administration experience, including:

- Event settings
- Team management
- Score controls
- Score history
- Modal forms

## Tablet

Tablet layouts must provide:

- Touch-friendly controls
- Reduced column layouts
- Responsive modals
- No horizontal page overflow

## Mobile Phone

Mobile layouts must provide:

- Single-column layouts where appropriate
- Large touch targets
- Wrapped score controls
- Responsive team cards
- Full-width or near-full-width modal interfaces
- No horizontal page overflow

Responsive behaviour will continue to be tested throughout development rather than being added only at the end.

---

# Current Technology Stack

## Frontend

- React
- TypeScript
- Vite
- CSS
- ESLint

## Development Tools

- IntelliJ IDEA
- Git
- GitHub
- npm

---

# Planned Backend Technology

The backend will be developed as a separate project.

Planned technologies include:

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- Jakarta Validation
- PostgreSQL
- JWT authentication

The backend will use object-oriented programming with a lightweight domain-oriented structure.

Full enterprise Domain-Driven Design is not currently required for the size of this application.

---

# Planned Architecture

The completed application is intended to follow this structure:

```text
React Frontend
      |
      | HTTP / REST
      |
      v
Spring Boot Backend
      |
      | JPA
      |
      v
PostgreSQL Database
```

Real-time communication will later be added so that score changes made by an administrator can appear on public scoreboard devices without requiring a manual refresh.

For example:

```text
Admin Laptop / Phone
        |
        | Score update
        v
Spring Boot
        |
        | Persist
        v
PostgreSQL
        |
        | Live update
        v
TV / Tablet / Phone Scoreboards
```

---

# Backend Structure

The planned Spring Boot backend will use packages similar to:

```text
com.friendslikethese.backend
|
|-- controller
|-- service
|-- repository
|-- domain
|-- dto
|-- security
|-- config
`-- exception
```

The basic application flow will be:

```text
Controller
    |
    v
Service
    |
    v
Repository
    |
    v
Database
```

Controllers will handle HTTP communication.

Services will contain application and business logic.

Repositories will handle persistence.

Domain classes will represent important application concepts such as teams, score events, users and event settings.

---

# Planned Backend Features

The backend development phase will introduce:

- Persistent teams
- Persistent scores
- Persistent score history
- Persistent event settings
- Team CRUD operations
- Score adjustment API
- Reset score operations
- Authentication
- Authorisation
- Administrator accounts
- Validation
- Centralised exception handling
- Real-time scoreboard synchronisation
- Registration integration

---

# Planned API

The first backend milestone will provide team-management endpoints similar to:

```text
GET    /api/teams
POST   /api/teams
PUT    /api/teams/{id}
DELETE /api/teams/{id}
```

Additional endpoints will later support:

```text
/api/scores
/api/score-events
/api/event-settings
/api/auth
```

The final endpoint structure will be determined as the backend domain is implemented.

---

# Frontend Structure

The current frontend is organised approximately as follows:

```text
src/
|
|-- assets/
|   `-- branding/
|
|-- components/
|   |
|   |-- admin/
|   |   |-- AddTeamForm
|   |   |-- AdminDashboard
|   |   |-- AdminEventSettings
|   |   |-- AdminTeamList
|   |   |-- DeleteTeamDialog
|   |   |-- EditTeamForm
|   |   |-- ScoreAdjustmentForm
|   |   |-- ScoreHistory
|   |   `-- UndoDeleteToast
|   |
|   |-- layout/
|   |   `-- AppHeader
|   |
|   |-- scoreboard/
|   |   |-- Countdown
|   |   |-- RegistrationQR
|   |   `-- Scoreboard
|   |
|   `-- team/
|       |-- MedalBadge
|       |-- TeamCard
|       `-- TeamList
|
|-- config/
|
|-- models/
|
|-- styles/
|
|-- App.tsx
`-- main.tsx
```

---

# State Management

During the frontend development phase, the primary application state is currently held in React.

This includes:

- Teams
- Scores
- Score events
- Registration deadline
- Delete/undo state
- Current application view

This is temporary.

Once backend integration begins, the Spring Boot backend and PostgreSQL database will become the authoritative source for persistent application data.

React will then request and update data through the backend API.

---

# Data Rules

The rebuild follows several important application rules.

## Scores

A team's score must not fall below zero.

## Score History

Meaningful score changes should produce an auditable score event.

## Team Deletion

Team deletion requires confirmation.

An accidental deletion can currently be undone for a short period.

## Score Reset

Resetting all scores requires confirmation because it affects every team.

## Shared Data

The public scoreboard and admin dashboard must ultimately use the same authoritative backend data.

---

# Local Development

## Requirements

Install:

- Node.js
- npm

Clone or switch to the frontend rebuild branch.

Install dependencies:

```bash
npm install
```

Start the Vite development server:

```bash
npm run dev
```

The development server normally runs at:

```text
http://localhost:5173
```

Run ESLint:

```bash
npm run lint
```

Create a production build:

```bash
npm run build
```

---

# Git Branches

The repository currently uses:

## `main`

Contains the original Friends Like These implementation.

## `react-rebuild`

Contains the current React and TypeScript rebuild.

The original application is intentionally preserved while the new version is being developed.

---

# Backend Repository

The Spring Boot backend will be developed as a separate IntelliJ project rather than inside the React frontend directory.

The frontend and backend will therefore have separate:

- dependency management
- build processes
- development servers
- project structures

During local development:

```text
React
http://localhost:5173

Spring Boot
http://localhost:8080

PostgreSQL
localhost:5432
```

---

# Cloud Direction

Azure is the planned cloud platform for the backend.

The intended production architecture is:

```text
Frontend
    |
    v
Spring Boot API
Azure App Service
    |
    v
Azure Database for PostgreSQL
```

The frontend deployment platform will be finalised during the deployment phase.

---

# Planned DevOps

Later development phases are intended to include:

- Docker
- GitHub Actions
- Automated builds
- Automated tests
- Backend deployment
- Environment configuration
- Production database configuration

---

# Development Roadmap

The current development sequence is:

1. Complete frontend foundation
2. Create Spring Boot backend
3. Configure PostgreSQL
4. Implement Team domain
5. Implement Team CRUD API
6. Implement scoring domain
7. Persist score history
8. Persist event settings
9. Implement authentication
10. Implement authorisation
11. Connect React to Spring Boot
12. Add real-time score synchronisation
13. Add registration integration
14. Perform multi-device testing
15. Add Docker
16. Add CI/CD
17. Deploy backend and database to Azure

See:

`docs/DEVELOPMENT_ROADMAP.md`

for more detail.

---

# Documentation

Additional project documentation is available in:

```text
docs/
|-- ARCHITECTURE.md
|-- DEVELOPMENT_ROADMAP.md
`-- FRONTEND.md
```

---

# Current Limitations

The current rebuild is still frontend-driven.

Teams, scores, score history and event settings are currently stored in memory.

This means refreshing the application can reset temporary application state.

Backend persistence has not yet been implemented.

Authentication has not yet been implemented.

Real-time communication between separate devices has not yet been implemented.

These limitations will be addressed during the Spring Boot backend phase.

---

# Repository

GitHub repository:

`Masego911/Friends-Like-These`

Active rebuild branch:

`react-rebuild`
