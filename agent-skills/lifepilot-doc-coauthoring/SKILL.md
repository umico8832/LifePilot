---
name: lifepilot-doc-coauthoring
description: LifePilot documentation coauthoring workflow for drafting, revising, or reviewing project docs such as PRD, roadmap, architecture, API design, database design, testing notes, decision log, backlog, current state, handoff, and next-chat prompts. Use when the user asks to write docs, update specs, clarify product decisions, capture handoff context, or keep Agent-facing documentation consistent.
---

# LifePilot Doc Coauthoring

Use this skill to keep LifePilot documentation useful for both humans and future agents.

Inspired by Anthropic's `doc-coauthoring` workflow, rewritten for this repository's document set.

## Document Routing

- Product scope: `docs/PRODUCT_STRATEGY.md`, `docs/PRD.md`, `docs/ROADMAP.md`
- Architecture: `docs/ARCHITECTURE.md`
- Database: `docs/DB_DESIGN.md`
- API: `docs/API_DESIGN.md`
- Testing: `docs/TESTING.md`
- Git workflow: `docs/AGENT_GIT_RULES.md`
- Autonomous process: `docs/AUTO_DEV_PROTOCOL.md`, `AGENTS.md`
- Current execution state: `docs/CURRENT_STATE.md`
- Task pool: `docs/BACKLOG.md`
- Handoff: `docs/HANDOFF.md`, `docs/NEXT_CHAT_PROMPT.md`
- Recent history: `docs/RECENT_HISTORY.md`
- Full development history: `docs/CHANGELOG_AGENT.md`, `docs/changelog/`
- Decisions: `docs/DECISION_LOG.md`

## Workflow

1. Identify the document's purpose and audience before editing.
2. Read the target document and any directly related docs. Do not infer product changes from memory alone.
3. Make the smallest durable change that keeps the docs internally consistent.
4. If the edit changes scope, architecture, data model, API behavior, workflow rules, or external-service policy, update `docs/DECISION_LOG.md`.
5. If the edit reflects completed work or a new handoff state, update `docs/CURRENT_STATE.md`, `docs/HANDOFF.md`, `docs/NEXT_CHAT_PROMPT.md`, and `docs/CHANGELOG_AGENT.md` as appropriate, then run `python3 scripts/agent_changelog_archive.py`.
6. Check references after editing with `rg` so renamed tasks, files, or claims do not drift.

## Writing Rules

- Be concrete about what exists now versus what is planned.
- Do not claim business modules are complete until code and verification exist.
- Keep `AGENTS.md` stable and long-lived. Put transient state in `docs/CURRENT_STATE.md`.
- Keep backlog entries executable: priority, status, phase, goal, files/modules, acceptance criteria, blocking status, and suggested next task.
- Use Chinese for project docs unless the surrounding file is English-only.
- Prefer short sections and lists future agents can scan quickly.

## Review Checklist

Before finishing a documentation task, verify:

- Required docs still point to each other correctly.
- Current state, backlog, handoff, and changelog agree.
- Any new constraint is reflected where future agents will read it.
- No real secrets, personal credentials, or external paid-service commitments were introduced.
