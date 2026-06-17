---
name: lifepilot-api-data-contract
description: LifePilot API and data contract workflow for designing or reviewing REST endpoints, request/response DTOs, MySQL tables, Flyway migrations, space-scoped data ownership, and documentation consistency. Use when changing `docs/API_DESIGN.md`, `docs/DB_DESIGN.md`, backend controller contracts, database migrations, or frontend API clients under `frontend/src/api/**`.
---

# LifePilot API Data Contract

Use this skill to keep LifePilot's API, database, frontend client, and documentation aligned.

## Required Reading

Read these before contract work:

- `docs/API_DESIGN.md`
- `docs/DB_DESIGN.md`
- `docs/ARCHITECTURE.md`
- Existing backend controller/service/mapper code for the module.
- Existing frontend API client and type files under `frontend/src/api` and `frontend/src/types`.

## Contract Workflow

1. Identify the resource boundary: user, space, transaction, category, shopping list, inventory item, todo, AI draft, or statistics.
2. Decide ownership: user-scoped or space-scoped. Business life data should be space-scoped unless docs say otherwise.
3. Define the database table and constraints before coding the API.
4. Define REST endpoints and methods in `docs/API_DESIGN.md`.
5. Define request/response DTOs with stable names and validation rules.
6. Implement backend controller/service/mapper and Flyway migration.
7. Implement frontend API client/types only after backend contract is clear.
8. Test both success and failure paths.
9. Update docs and changelog.

## API Rules

- Use `/api/**` paths.
- Use nested paths for space-owned resources: `/api/spaces/{spaceId}/...`.
- Use `GET` for reads, `POST` for create/actions, `PATCH` for partial updates, and `DELETE` for deletion.
- Return the existing unified `ApiResponse` shape.
- Keep error codes from `docs/API_DESIGN.md`: `VALIDATION_ERROR`, `UNAUTHORIZED`, `FORBIDDEN`, `NOT_FOUND`, `CONFLICT`, `BUSINESS_ERROR`, `INTERNAL_ERROR`.
- Do not expose password hashes, internal IDs unrelated to the resource, or provider secrets.
- Prefer explicit action endpoints only when a state transition is clearer than a generic update, such as `complete` or AI draft confirmation.

## Database Rules

- Use Flyway migrations for every schema change.
- Use `BIGINT` IDs unless the project adopts another standard.
- Include `created_at` and `updated_at`.
- Add `space_id` or equivalent ownership to business tables.
- Add indexes for ownership lookups such as `space_id`, `user_id`, and common list filters.
- Do not rely on frontend filtering for authorization or tenancy isolation.
- Do not edit old migrations after they may have been applied.

## Frontend Client Rules

- Put HTTP functions in `frontend/src/api/<module>.ts`.
- Put shared TypeScript shapes in `frontend/src/types/<module>.ts` when they are reused.
- Keep frontend field names aligned with backend JSON.
- Handle loading, empty, error, and success states in views or stores.
- Keep the current space ID explicit in API calls for space-owned resources.

## Review Checklist

Before finishing contract work, verify:

- `docs/API_DESIGN.md` matches controller paths.
- `docs/DB_DESIGN.md` matches migrations.
- Frontend API clients call real backend paths.
- Tests cover unauthorized and forbidden access where applicable.
- `docs/CURRENT_STATE.md`, `docs/HANDOFF.md`, and `docs/CHANGELOG_AGENT.md` reflect the real state.
