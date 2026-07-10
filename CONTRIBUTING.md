# Contributing to HireTrack

## Local setup

See the Quick Start section in [README.md](README.md) — it covers both the
backend (Spring Boot) and frontend (React + Vite) setup.

## Branching & commits

- Branch per feature: `feat/kanban-drag-drop`, `fix/stage-move-race-condition`.
- Never push directly to `main`.
- Use [Conventional Commits](https://www.conventionalcommits.org/): `feat:`,
  `fix:`, `docs:`, `refactor:`, `test:`, `chore:`.
- Keep commits small and scoped to one logical change — the git log should
  read as a story of how the feature was built, not one giant diff.

## Before opening a PR

```bash
# Backend
cd backend
mvn verify

# Frontend
cd frontend
npm run lint
npm run build
```

Both must pass locally before you open a PR — CI runs the same checks and
will block merge otherwise.

## Pull requests

- Describe **what changed and why**, not just what changed.
- Link the issue it closes, if any.
- Include screenshots for any UI change.
