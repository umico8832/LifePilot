---
name: lifepilot-ai-mock-provider
description: LifePilot AI mock provider workflow for implementing safe, deterministic, structured AI-like features such as natural-language transaction parsing, shopping list drafts, todo drafts, and monthly report drafts. Use when working under `backend/ai`, AI endpoints in `docs/API_DESIGN.md`, frontend AI confirmation flows, or any feature that must simulate AI output without real API keys or paid external providers.
---

# LifePilot AI Mock Provider

Use this skill to build AI-shaped features safely before real provider integration exists.

## Required Reading

Read:

- `docs/ARCHITECTURE.md`
- `docs/API_DESIGN.md`
- `docs/DB_DESIGN.md`
- `docs/PRODUCT_STRATEGY.md`
- `docs/AUTO_DEV_PROTOCOL.md`
- Existing backend `common`, `security`, and target business module code.

## Safety Rules

- Do not add real API keys, provider secrets, paid external services, or network AI calls.
- Do not present mock output as real model output.
- Keep mock behavior deterministic enough for tests.
- AI endpoints should return drafts. Users must confirm before business tables are written.
- Avoid medical, legal, investment, payment, automatic purchase, or diagnosis-like recommendations.
- Log only safe metadata. Do not store raw secrets or sensitive credentials in `ai_call_log`.

## Provider Shape

Prefer this backend shape when AI implementation starts:

- `AiProvider` interface with scenario-specific methods.
- `MockAiProvider` default implementation.
- DTOs for structured input and output.
- Controller endpoints under `/api/ai/**`.
- Service layer that validates auth and space membership before producing drafts.
- Tests for parser output, invalid input, auth, and fallback behavior.

Do not wire a real provider until project docs and user decision explicitly allow it.

## Structured Output Rules

Every mock response should include:

- A stable machine-readable payload.
- A confidence or review flag when useful.
- A human-readable note only as supporting context.
- Enough fields for the user to confirm, edit, or reject before saving.

Example transaction draft shape:

```json
{
  "type": "expense",
  "amount": "32.50",
  "currency": "CNY",
  "occurredAt": "2026-06-17T12:00:00Z",
  "merchant": "超市",
  "categoryName": "食品日用",
  "note": "午餐和日用品",
  "needsReview": true
}
```

## Mock Parsing Guidelines

- Use simple deterministic parsing rules and document their limits in tests or code comments.
- Prefer conservative results with `needsReview: true` over guessing silently.
- Keep currency, dates, categories, and space ownership explicit.
- For ambiguous input, return a draft with missing fields and an actionable validation message rather than inventing facts.

## Frontend Flow

AI UI should:

- Show the parsed draft in editable fields.
- Require explicit user confirmation before calling normal business write APIs.
- Make mock/provider status visible in dev-facing text or logs where appropriate.
- Handle empty, invalid, loading, and error states.

Use `lifepilot-frontend-design` and `lifepilot-webapp-testing` when the AI flow includes user-facing UI.

## Verification

Run:

```bash
cd backend && mvn test
cd frontend && npm run build
```

Add tests for representative phrases, ambiguous phrases, invalid input, and no-provider fallback. Record skipped real-provider tests as intentionally out of scope.
