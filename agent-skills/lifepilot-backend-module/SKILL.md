---
name: lifepilot-backend-module
description: LifePilot backend module workflow for implementing Spring Boot business modules such as space, finance, shopping, inventory, todo, statistics, and user features. Use when adding or changing backend packages under `backend/src/main/java/com/lifepilot/**`, Flyway migrations, MyBatis-Plus mappers, service rules, controller endpoints, authentication, or space-scoped authorization.
---

# LifePilot Backend Module

Use this skill to build backend features that are consistent with LifePilot's Spring Boot, Security, MyBatis-Plus, Flyway, and MySQL patterns.

## Required Reading

Before editing, read:

- `docs/ARCHITECTURE.md`
- `docs/API_DESIGN.md`
- `docs/DB_DESIGN.md`
- `docs/TESTING.md`
- `docs/AGENT_REVIEW_CHECKLIST.md`
- Existing code in `backend/src/main/java/com/lifepilot/common`, `security`, `auth`, and the target package.

For space-scoped modules, read or create the `space` model before writing dependent business data.

## Module Shape

Prefer this package shape when a module needs persistence and HTTP APIs:

- Entity/model: `backend/src/main/java/com/lifepilot/<module>/<Name>.java`
- Mapper: `backend/src/main/java/com/lifepilot/<module>/<Name>Mapper.java`
- Service: `backend/src/main/java/com/lifepilot/<module>/<Name>Service.java`
- Controller: `backend/src/main/java/com/lifepilot/<module>/<Name>Controller.java`
- DTOs: keep near the module unless the project later creates a shared DTO package.
- Migration: `backend/src/main/resources/db/migration/V<N>__<description>.sql`
- Tests: `backend/src/test/java/com/lifepilot/<module>/**`

Follow existing names and annotations before introducing new conventions.

## Implementation Rules

- Return `ApiResponse` from controllers.
- Throw `BusinessException` for business failures and use existing error codes where possible.
- Keep validation at request boundaries with Jakarta Validation.
- Never trust client-provided user IDs for ownership. Derive current user from `CurrentUserPrincipal`.
- For any business data beyond user profile, require space membership checks before read or write.
- Store ownership with `space_id` or the current physical equivalent documented in `docs/DB_DESIGN.md`.
- Use Flyway for schema changes. Do not hand-edit an already-applied migration.
- Keep mock behavior explicit. Do not present temporary data as real business state.

## Authorization Checklist

For every endpoint, decide:

- Is it public, authenticated, or space-scoped?
- Which role can perform it: owner, admin, member, or viewer?
- Does list/detail/update/delete verify the current user belongs to the target space?
- Are forbidden and not-found cases distinguishable enough without leaking unrelated data?

For P0 work, prefer conservative access: owner/admin for management, member for ordinary writes, viewer for reads.

## Testing

At minimum, run:

```bash
cd backend && mvn test
```

Add tests for:

- Happy path.
- Validation failure.
- Unauthenticated access when applicable.
- Forbidden access to a space the user does not belong to.
- Duplicate or conflict cases when the model has unique constraints.

When database behavior matters, verify Flyway migrations through the test profile or real MySQL when the task requires it.

## Documentation Updates

When behavior changes, update the relevant docs:

- `docs/API_DESIGN.md` for endpoints and response shape.
- `docs/DB_DESIGN.md` for schema/model changes.
- `docs/CURRENT_STATE.md` and `docs/BACKLOG.md` for task state; append history to `docs/CHANGELOG_AGENT.md` and refresh `docs/RECENT_HISTORY.md` with `python3 scripts/agent_changelog_archive.py`.
- `docs/DECISION_LOG.md` for notable architecture, schema, or security decisions.
