# Architecture

## Overview

HireTrack is a two-service app: a Spring Boot REST API (Java 17) backed by
PostgreSQL, and a React + TypeScript SPA that talks to it over JSON.

```
┌─────────────────┐        JWT-authenticated REST         ┌──────────────────┐
│  React (Vite)    │ ─────────────────────────────────────▶│  Spring Boot API │
│  localhost:5173   │◀───────────────────────────────────── │  localhost:8080  │
└─────────────────┘                                        └────────┬─────────┘
                                                                      │ Spring Data JPA
                                                                      ▼
                                                             ┌──────────────────┐
                                                             │   PostgreSQL      │
                                                             └──────────────────┘
```

## Data model

See [plan.md] equivalent in the schema comments and `V1__init.sql` for the
full DDL. In short:

```
organizations 1─* users
organizations 1─* jobs
jobs 1─* pipeline_stages
organizations 1─* candidates
jobs 1─* applications *─1 candidates
applications 1─* activity_log_entries
```

`applications` is the junction between a job and a candidate, and carries the
Kanban state (`current_stage_id`, `status`). It uses JPA optimistic locking
(`@Version`) so two recruiters dragging the same card at the same time get a
clean 409 instead of silently clobbering each other's move.

## Auth & authorization

- Stateless JWT access tokens (HS256), issued on register/login.
- Passwords hashed with Argon2id.
- Every request re-derives a `UserPrincipal` (userId, orgId, role) from the
  token via a `OncePerRequestFilter` — no server-side session state.
- **Every** data-access method re-verifies that the requested resource
  belongs to the caller's organization (`loadScopedJob`, `loadScopedApplication`,
  etc.) before returning or mutating it. A role claim alone is never treated
  as sufficient — ownership is checked per request, per the plan.md roles matrix.
- Cross-org access attempts return `404`, not `403`, so we don't leak whether
  a resource exists in another org.

## Known trade-offs (called out deliberately, not accidental gaps)

1. **Multi-tenancy is enforced at the query/service layer**, not via Postgres
   row-level security. Acceptable for this scope; would need RLS or a
   separate schema-per-tenant model to harden further for production.
2. **Rate limiting on auth endpoints** (5 attempts / 15 min) is flagged in
   `AuthService` but not yet wired to a real limiter (e.g. Bucket4j or a
   gateway-level solution) — a clear next step before this handles real
   traffic.
3. **Dashboard aggregation queries loop per-job** (N+1) rather than using a
   single JPQL `GROUP BY`. Flagged in code; fine at demo scale, not at
   thousands of jobs.
4. **Interviews and structured scorecards** (from the original plan.md) are
   not implemented in this milestone — the schema and roles support them,
   but the entities/endpoints are the clearly-scoped next milestone rather
   than being half-built here.

## Frontend structure

- `src/context/AuthContext.tsx` — token + user state, persisted to
  `localStorage`, attached to every request via an axios interceptor.
- `src/pages/JobBoardPage.tsx` — the Kanban board. Drag-and-drop uses the
  native HTML5 DnD API (no extra dependency) with optimistic UI: the card
  moves immediately on drop, and rolls back with an error banner if the
  server rejects it (e.g. a stale-version conflict).
- Every async page follows the same four-state pattern: loading (skeleton),
  error (retry button), empty (actionable CTA), success.
