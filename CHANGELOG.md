# Changelog

All notable changes to this project are documented here, in the
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/) format.

## [Unreleased]
### Added
- Interviews and structured scorecards (planned next milestone).

## [0.1.0] - 2026-07-10
### Added
- Auth (register/login) with JWT and Argon2id password hashing.
- Role-based access control (Admin, Recruiter, Interviewer) enforced server-side.
- Jobs CRUD with auto-seeded default pipeline stages.
- Candidates CRUD.
- Kanban pipeline board with drag-and-drop stage moves, optimistic UI, and
  optimistic-locking conflict handling.
- Candidate rejection flow with required reason.
- Activity log / audit trail per application.
- Workspace dashboard: open jobs, active/hired/rejected counts, average
  time-to-hire, and a pipeline funnel chart.
