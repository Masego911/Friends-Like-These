# 🎮 Friends Like These: Campus Key Edition

A real-time event management and live scoring platform developed for the **Campus Key Friends Like These** competition.

The application enables event organisers to manage teams, update scores in real time, synchronise participant registrations, and display a live leaderboard across multiple devices. It combines responsive web technologies with cloud services to deliver a seamless experience for both organisers and participants.

---

## Live Demo

🔗 https://friendsliketheseck.netlify.app/

---

## Overview

Friends Like These: Campus Key Edition was developed to simplify the management of live student competitions.

Instead of manually tracking scores and registrations, the application provides:

- Live team leaderboard
- Real-time score synchronisation
- Automatic team registration import
- Administrator dashboard
- Registration countdown timer
- QR code registration
- Responsive display suitable for TVs, laptops, tablets and mobile devices

The project demonstrates practical software engineering principles through the integration of cloud services, responsive interface design, and real-time data synchronisation.

---

# Features

## Live Leaderboard

- Real-time score updates
- Automatic ranking
- Gold, Silver and Bronze highlighting
- Live score animations
- Responsive scoreboard

---

## Administrator Dashboard

Secure administrator panel allowing organisers to:

- Add teams
- Remove teams
- Increase scores
- Deduct scores
- Reset all scores
- Reload registration data

---

## Team Registration

Participants register using:

- Google Forms
- QR Code
- Automatic Google Sheets integration

New teams automatically become available inside the administration system.

---

## Countdown Timer

Displays the remaining registration time.

Features include:

- Live countdown
- Automatic registration closure
- Visual warning when registration is about to close

---

## Firebase Integration

Scores are stored inside Firebase Realtime Database.

Benefits include:

- Instant synchronisation
- Multiple device support
- Cloud persistence
- Automatic updates

---

## Google Sheets Integration

The application automatically imports team information from Google Sheets.

This removes the need for organisers to manually capture participant information.

---

## Responsive Design

Optimised for

- Desktop
- Laptop
- Tablet
- Mobile
- Television displays

---

# Technologies Used

## Frontend

- HTML5
- CSS3
- JavaScript (ES6)

---

## Backend Services

- Firebase Realtime Database

---

## External APIs

- Google Sheets API
- Google Forms
- QR Code Generator API

---

## Hosting

- Netlify

---

## Development Tools

- Visual Studio Code
- Git
- GitHub

---

# Software Engineering Principles

This project applies several software engineering concepts including:

- Modular design
- Separation of concerns
- Responsive UI design
- Cloud-based architecture
- Real-time data synchronisation
- External API integration
- Event-driven programming

---

# Project Architecture

```
                 Users
                    │
                    ▼
        Friends Like These Website
                    │
     ┌──────────────┼──────────────┐
     ▼              ▼              ▼
Google Forms   Firebase DB   Google Sheets
     │              │              │
     └──────────────┴──────────────┘
                    │
                    ▼
           Live Scoreboard
```

---

# Main Components

## Scoreboard

Displays:

- Team rankings
- Live scores
- Team members
- Registration countdown
- QR registration

---

## Admin Panel

Allows administrators to:

- Authenticate
- Manage teams
- Update scores
- Reset competition

---

## Registration Module

Responsible for:

- Team registration
- Google Forms integration
- Google Sheets synchronisation

---

## Score Synchronisation

Uses Firebase Realtime Database to provide:

- Instant updates
- Live score changes
- Automatic refresh

---

# Folder Structure

```
FriendsLikeThese/

│
├── README.md
├── FRONTEND_DOCUMENTATION.md
├── BACKEND_DOCUMENTATION.md
├── SOFTWARE_ARCHITECTURE.md
├── DATABASE_DESIGN.md
├── API_DOCUMENTATION.md
├── LICENSE
│
├── frontend/
│   ├── html/
│   ├── css/
│   ├── javascript/
│   └── assets/
│
├── backend/
│   ├── controllers/
│   ├── services/
│   ├── models/
│   └── utilities/
│
├── UML/
│
├── screenshots/
│
└── docs/
```

---

# Installation

Clone the repository.

```bash
git clone https://github.com/yourusername/FriendsLikeThese.git
```

Navigate into the project.

```bash
cd FriendsLikeThese
```

Open the project using your preferred IDE.

Configure:

- Firebase credentials
- Google Sheets API
- Registration form URL

Run the application.

---

# Future Improvements

Potential future enhancements include:

- User authentication
- Multiple competitions
- Tournament brackets
- Team statistics
- Historical score tracking
- Export to Excel
- Analytics dashboard
- Dark/Light themes
- Email notifications
- Attendance tracking
- AI-powered event insights

---

# Skills Demonstrated

## Software Engineering

- Object-Oriented Design
- Modular Programming
- MVC Principles
- Software Architecture
- Problem Solving

---

## Frontend Development

- HTML5
- CSS3
- JavaScript
- Responsive Design
- Animations

---

## Backend Integration

- Firebase
- REST APIs
- Cloud Synchronisation

---

## Data Management

- Google Sheets API
- JSON
- Data Parsing

---

## User Experience

- Responsive layouts
- Accessibility considerations
- Live feedback
- Dashboard design

---

# Learning Outcomes

This project strengthened my understanding of:

- Real-time application development
- Cloud database integration
- API communication
- Event-driven programming
- Responsive interface design
- Software architecture
- Frontend engineering
- User experience design
- Version control using Git

---

# Author

**Masego Madisha**

Final-year Bachelor of Commerce (Computer Science and Information Systems)

Nelson Mandela University

GitHub:
https://github.com/Masego911
---

# License

This project is licensed under the MIT License.

---

# Acknowledgements

Special thanks to:
- Campus Key

for providing the platforms and technologies that made this project possible.
