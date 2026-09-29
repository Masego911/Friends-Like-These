# Friends Like These Frontend

React and TypeScript frontend for the **Friends Like These Games Night Management & Live Scoring Platform**.

The frontend provides two distinct experiences:

- a public scoreboard for participants and spectators
- an authenticated administration interface for organisers running the game

The application communicates with the Friends Like These Spring Boot backend through a REST API.

---

## Technology Stack

| Area | Technology |
|---|---|
| Framework | React |
| Language | TypeScript |
| Build Tool | Vite |
| Styling | CSS |
| API Communication | REST |
| Backend | Java Spring Boot |
| Development Server | Vite |

---

# Frontend Responsibilities

The frontend is responsible for presenting the current game state and providing the interfaces required to operate a Friends Like These games night.

The backend remains the authoritative source for events, teams, rounds, scores, registration and authentication.

The frontend handles:

- Public leaderboard presentation
- Live game information
- Administrator navigation
- Live scoring controls
- Round controls
- Team management
- Registration management
- Game setup
- Previous-game presentation
- Authentication interfaces
- Responsive presentation
- Periodic synchronisation with the backend

---

# Application Views

The application currently has three primary views:

```text
Friends Like These
│
├── Public Scoreboard
│
├── Admin
│
└── Previous Game / History
```

Each view serves a different purpose while consuming data from the same backend.

---

# Public Scoreboard

The public scoreboard is the audience-facing part of Friends Like These.

It is designed to be displayed during a live games night on:

- TVs
- Projectors
- Laptops
- Tablets
- Mobile devices

The scoreboard displays information such as:

- Team rankings
- Team names
- Current scores
- Round information
- Game state
- Registration information

The public interface does not expose administrative controls.

---

# Live Scoreboard Synchronisation

The public scoreboard periodically retrieves the latest game state from the backend.

This allows score changes made through the administration interface to appear on the public display without manually refreshing the page.

The frontend retrieves information including:

```text
Current Event
     +
Leaderboard
     +
Rounds
     +
Round Standings
     +
Registration Status
```

and uses that information to construct the audience-facing scoreboard.

---

# Administration Interface

The administration interface is designed for the organiser actively running the games night.

The current navigation includes:

```text
Dashboard
Live Scoring
Teams
Registration
Game Setup
Previous Games
Settings
```

The admin interface is separate from the public scoreboard so operational controls are not visible to participants and spectators.

---

# Admin Dashboard

The dashboard provides an overview of the current game.

It surfaces information such as:

- Current game
- Game status
- Current round
- Number of rounds
- Participating teams
- Registration state

From the administration area, organisers can move between the different game-management functions.

---

# Live Scoring

The Live Scoring interface is designed to make score management quick during an active game.

Teams are presented in leaderboard order with controls for common score adjustments.

Quick scoring includes:

```text
-10   -5   -1   +1   +5   +10
```

Additional scoring controls are available when required.

The scoring interface displays:

- Rank
- Team
- Members
- Score
- Quick Score controls
- Additional actions

Score changes are sent to the backend and the returned server state is used to update the interface.

---

# Game Progress

Round controls are available directly from Live Scoring.

This allows an organiser to:

```text
Start Round
     ↓
Score Teams
     ↓
End Round
     ↓
Start Next Round
```

without leaving the scoring interface.

The same round state is shared with Game Setup.

The frontend does not independently decide whether scoring is valid. The backend also enforces the event and round lifecycle rules.

---

# Team Management

The Teams area provides administrative access to teams participating in the current game.

The interface works with backend team data including:

- Team name
- Team members
- Current score
- Team identifier
- Registration/import state

Changes made through the interface are persisted through the backend API.

---

# Registration

The Registration interface allows organisers to monitor team registration and Google Sheets synchronisation.

Registration data follows the wider system pipeline:

```text
Google Form
     ↓
Google Sheets
     ↓
Spring Boot
     ↓
SQL Server
     ↓
React Admin
```

The frontend can display:

- Registration status
- Synchronisation status
- Imported teams
- Registration information
- Synchronisation errors

Automatic backend synchronisation means organisers do not need to repeatedly import registration data manually.

---

# Game Setup

Game Setup provides controls relating to the current event.

The interface works with the backend event lifecycle:

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

The UI presents the actions appropriate to the current game state while the backend remains responsible for validating lifecycle transitions.

---

# Previous Games

The Previous Games interface allows organisers to inspect historical games.

Historical information can include:

- Game details
- Teams
- Team members
- Final standings
- Rounds
- Score activity

Historical games are presented as read-only information so previous results remain available without affecting the current game.

---

# Frontend State and API Synchronisation

Friends Like These deliberately avoids treating browser state as the permanent source of truth.

The typical data flow is:

```text
User Action
     ↓
React Component
     ↓
API Client
     ↓
Spring Boot
     ↓
SQL Server
     ↓
API Response
     ↓
React State
     ↓
Updated Interface
```

This is particularly important for scoring.

When an administrator changes a score, the frontend sends the adjustment to the backend and uses the authoritative response to update the displayed team.

---

# Polling

The frontend periodically refreshes game information so that separate screens remain synchronised during a live event.

This is important when:

```text
Admin Laptop
     |
     | score change
     v
Spring Boot
     |
     v
SQL Server
     |
     | refreshed game state
     v
Public Scoreboard
```

The public display therefore does not need to be running on the same device as the administration dashboard.

---

# Error Handling

Frontend data requests are designed so that an unrelated request failure does not unnecessarily destroy otherwise valid screen state.

For example, failure to retrieve one administrative dataset should not automatically be interpreted as an authentication failure.

The interface provides appropriate loading, unavailable and error states while communicating with the backend.

---

# Authentication

The frontend provides the interface for administrator login and logout.

Authentication itself is handled by the Spring Boot backend.

Administrative API requests require an authenticated administrator session.

The public scoreboard remains separate from protected administration functionality.

---

# Styling & Brand Direction

Friends Like These is designed as a games-night experience rather than a generic business dashboard.

The interface uses the Friends Like These visual identity across both public and administrative views.

The current visual direction includes:

- Strong event-focused typography
- White and light interface surfaces
- Dark navy text and feature areas
- Purple interface accents
- Teal score treatment
- Red live-game accents
- Multicolour Friends Like These branding

The multicolour treatment uses:

```text
Orange
Red
Purple
Blue
Teal
```

The public scoreboard and administration dashboard share the same product identity while remaining visually appropriate for their different purposes.

---

# Responsive Design

The frontend is designed to work across different screen sizes.

Important interfaces are intended for:

```text
Large event display
Desktop
Laptop
Tablet
Mobile
```

The administration interface prioritises efficient event operation, while the public scoreboard prioritises visibility and readability from a distance.

---

# Frontend Structure

The frontend currently lives at the root of the Friends Like These repository.

A simplified structure is:

```text
Friends-Like-These/
│
├── src/
│   ├── admin/
│   ├── api/
│   ├── components/
│   ├── models/
│   ├── App.tsx
│   └── ...
│
├── public/
├── package.json
├── vite.config.ts
├── FRONTEND.md
│
└── backend/
```

The Spring Boot backend is maintained separately under `/backend` while remaining part of the same Git repository.

---

# API Configuration

The frontend communicates with the Spring Boot backend through the configured API base URL.

For local development:

```text
VITE_API_BASE_URL=http://localhost:8080
```

The frontend development server normally runs on:

```text
http://localhost:5173
```

---

# Running the Frontend

## Requirements

Install:

- Node.js
- npm

From the repository root:

```powershell
npm install
```

Then start the Vite development server:

```powershell
npm run dev
```

---

# Production Build

Create a production build using:

```powershell
npm run build
```

Vite outputs the production frontend bundle for deployment.

---

# Current Development

The next major frontend development area is the **live audience scoreboard experience**.

The goal is for the public scoreboard to respond automatically to round state.

### Between Rounds

The audience sees:

```text
OVERALL STANDINGS
```

using cumulative game scores.

### When a Round Starts

When the administrator presses **Start Round**, the public display will transition automatically to:

```text
ROUND N
ROUND IN PROGRESS
```

followed by the leaderboard for that specific round.

Round scores will be presented independently from the cumulative overall standings.

### During the Round

Planned live effects include:

- Animated score changes
- Score count-up/count-down
- Temporary point-change indicators
- Smooth leaderboard position changes
- Row movement when rankings change

### When the Round Ends

When the administrator presses **End Round**, the audience display is planned to show:

```text
ROUND N COMPLETE
```

followed by a branded transition back to:

```text
OVERALL STANDINGS
```

The overall leaderboard will then reflect the updated cumulative scores.

---

# Motion & Accessibility

Animation is intended to increase the excitement of the live event without making the scoreboard difficult to follow.

Transitions should therefore remain short and purposeful.

Reduced-motion preferences will be respected so users who request reduced animation can still use the application comfortably.

---

# Frontend Roadmap

Planned frontend work includes:

- Round-aware public scoreboard
- Overall-to-round animated transitions
- Round-complete presentation
- Animated score changes
- Animated leaderboard movement
- Improved loading and error states
- Continued responsive optimisation
- Production deployment integration
- Analytics visualisation

---

# Related Documentation

Friends Like These is maintained as one full-stack application.

```text
README.md
    Whole application and project documentation

FRONTEND.md
    React frontend documentation

backend/README.md
    Spring Boot backend documentation
```

See the root `README.md` for the overall project and `/backend/README.md` for backend architecture, security, persistence, concurrency and API behaviour.

---

# Dedicated Smart TV Scoreboard

Friends Like These includes a dedicated audience-facing Smart TV scoreboard for live games nights.

## Live Applications

- Main application and administration: https://friendsliketheseck.netlify.app
- Smart TV scoreboard: https://friendslikethesetv.netlify.app
- Backend: Spring Boot API hosted on Microsoft Azure
- Database: Azure SQL

## TV Scoreboard

The TV application is separate from the administration interface. Organisers can manage the game from a computer or mobile device while the audience scoreboard runs independently on a Smart TV or large display.

Architecture:

Admin / Main Application
        |
        v
Spring Boot API
        |
        v
Azure SQL
        |
        v
Dedicated TV Scoreboard

The TV scoreboard provides:

- Live team standings
- Automatic score updates
- Overall standings
- Round-specific standings
- Current round information
- Round-start presentation
- Round-winner presentation
- Registration countdown
- Registration QR code
- Friends Like These branding
- Connection and reconnection states

The TV contains no administrative controls.

## Smart TV Compatibility

The dedicated TV frontend is maintained under the `/tv` directory as a separate React, TypeScript and Vite application.

Its production build includes legacy JavaScript output and browser polyfills using `@vitejs/plugin-legacy` to improve compatibility with older Smart TV browser engines.

The TV deployment uses a Netlify API proxy to communicate with the Azure-hosted backend while keeping the browser requests same-origin.

## TV Deployment

The dedicated production scoreboard is deployed separately from the main application:

https://friendslikethesetv.netlify.app

This separation allows the TV experience to be optimised for large displays and Smart TV compatibility without affecting the administration interface.

## TV Project Structure

Friends-Like-These/
|
|-- src/                 Main React application
|-- tv/                  Dedicated Smart TV application
|   |-- src/
|   |-- package.json
|   |-- vite.config.ts
|   `-- netlify.toml
|
`-- backend/             Spring Boot backend

