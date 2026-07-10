# HireTrack

> A lightweight applicant tracking system: a Kanban pipeline, role-based
> access, and hiring analytics for small recruiting teams.

[![CI](https://github.com/YOUR_USERNAME/hiretrack/actions/workflows/ci.yml/badge.svg)](https://github.com/YOUR_USERNAME/hiretrack/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**Live demo →** _add your deployed URL here_

Built as part of the [Digital Heroes](https://digitalheroes.dev) Full Stack
Developer trial task.

## Features

- Email/password auth with Argon2id hashing and JWT sessions
- Role-based access control — Admin, Recruiter, Interviewer — enforced
  server-side, not just hidden in the UI
- Job postings with an auto-seeded, customizable pipeline
  (Sourced → Screening → Interview → Offer → Hired/Rejected)
- Drag-and-drop Kanban board with optimistic UI and conflict-safe
  concurrent editing (optimistic locking)
- Candidate rejection flow with a required reason
- Full audit trail per candidate (every stage move, rejection, and note)
- Workspace dashboard: open jobs, active/hired/rejected counts, average
  time-to-hire, and a live pipeline funnel

## Tech stack

**Backend:** Java 17 · Spring Boot 3 · Spring Security · Spring Data JPA ·
PostgreSQL · Flyway · JWT (jjwt)
**Frontend:** React 18 · TypeScript · Vite · Tailwind CSS · React Router ·
Axios

## Quick start

### 1. Database

```bash
docker compose up -d   # starts Postgres on localhost:5432
```

### 2. Backend

```bash
cd backend
cp .env.example .env    # then fill in values (defaults work with docker compose)
mvn spring-boot:run
# API runs at http://localhost:8080
```

Flyway runs the migration automatically on startup — no manual `db:migrate`
step needed.

### 3. Frontend

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
# App runs at http://localhost:5173
```

### 4. Try it

1. Open http://localhost:5173/register and create a workspace — the first
   user is automatically an Admin.
2. Create a job (a default pipeline is seeded automatically).
3. Add a candidate, then add them to the job's pipeline.
4. Drag their card across stages on the Kanban board.
5. Check the Dashboard tab for the funnel and time-to-hire stats.

## Environment variables

**Backend** (`backend/.env.example`)

| Variable | Description |
|---|---|
| `DATABASE_URL` | Postgres JDBC connection string |
| `DATABASE_USERNAME` / `DATABASE_PASSWORD` | DB credentials |
| `JWT_SECRET` | HS256 signing secret — must be a long random string in production |
| `JWT_ACCESS_TTL_MIN` | Access token lifetime in minutes |
| `CORS_ALLOWED_ORIGINS` | Comma-separated list of allowed frontend origins |

**Frontend** (`frontend/.env.example`)

| Variable | Description |
|---|---|
| `VITE_API_URL` | Base URL of the backend API |

## Architecture

See [docs/architecture.md](docs/architecture.md) for the data model, auth
design, and — importantly — the trade-offs made deliberately for this scope
(and what the honest next steps are).

## Testing

```bash
# Backend
cd backend && mvn test

# Frontend build/typecheck
cd frontend && npm run build
```

## Roadmap

- [x] Auth, RBAC, Jobs, Candidates, Kanban pipeline, Dashboard
- [ ] Interview scheduling + structured scorecards
- [ ] CSV export of pipeline views
- [ ] Real rate limiting on auth endpoints
- [ ] SEO pass on a public landing page (currently app-only)

## Screenshots

### Workspace registration
![Register](docs/screenshots/Register.png)

### Kanban pipeline
![Pipeline](docs/screenshots/Pipeline.png)

### Dashboard analytics
![Dashboard](docs/screenshots/Dashboard.png)

### Candidate List
![Rejection](docs/screenshots/Candidates.png)

### Job List
![Jobs](docs/screenshots/Jobs.png)

## License

MIT — see [LICENSE](LICENSE).

---

Built for the Digital Heroes Full Stack Developer trial task.
