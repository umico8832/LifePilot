---
name: lifepilot-auto-dev
description: LifePilot autonomous development workflow for continuing implementation from the backlog, selecting the highest-priority unblocked task, reading required docs, making scoped code changes, running backend/frontend verification, updating state documents, and deciding whether to continue or stop. Use when the user asks to continue LifePilot development, implement the next task, pick up from handoff, or work according to the repo's Agent protocol.
---

# LifePilot Auto Dev

Use this skill to run the repository's autonomous development loop without losing state.

## Required Reading

Read these first, in order:

1. `AGENTS.md`
2. `docs/AUTO_DEV_PROTOCOL.md`
3. `docs/CURRENT_STATE.md`
4. `docs/BACKLOG.md`
5. `docs/HANDOFF.md`
6. `docs/CHANGELOG_AGENT.md`

Then read task-specific docs:

- Product/scope: `docs/PRODUCT_STRATEGY.md`, `docs/PRD.md`, `docs/ROADMAP.md`
- Architecture: `docs/ARCHITECTURE.md`
- Database: `docs/DB_DESIGN.md`
- API: `docs/API_DESIGN.md`
- Testing: `docs/TESTING.md`
- Git: `docs/AGENT_GIT_RULES.md`
- Self-review: `docs/AGENT_REVIEW_CHECKLIST.md`
- Decisions: `docs/DECISION_LOG.md`

## Development Loop

1. Select the highest-priority, lowest-phase, unblocked `todo` task in `docs/BACKLOG.md`.
2. Mark the task `in_progress` only when you are actually starting it.
3. Read relevant existing code before editing.
4. Implement the smallest complete change that satisfies the acceptance criteria.
5. Add or update tests proportional to the risk.
6. Run required verification:

```bash
cd backend && mvn test
cd frontend && npm run build
```

7. Use `lifepilot-webapp-testing` when the task changes meaningful UI or browser behavior.
8. Update docs:

- `docs/BACKLOG.md`: task status
- `docs/CURRENT_STATE.md`: current phase, completed work, blockers, next task, verification
- `docs/CHANGELOG_AGENT.md`: append a dated entry
- `docs/HANDOFF.md`: keep handoff current
- `docs/NEXT_CHAT_PROMPT.md`: keep continuation prompt current
- Related design docs and `docs/DECISION_LOG.md` when behavior or architecture changes

9. Read `docs/AGENT_REVIEW_CHECKLIST.md` and self-review before final response.
10. Continue to the next task unless a stop condition is triggered.

## Stop Conditions

Stop only for the conditions in `docs/AUTO_DEV_PROTOCOL.md`, including:

- User explicitly asks to stop.
- A major product decision is required.
- Real API keys, paid services, passwords, or external accounts are needed.
- Destructive database changes or irreversible data operations are required.
- Git conflict or repeated test failure cannot be safely resolved.
- All current-stage P0/P1/P2 tasks are complete.
- Context is close to the limit; update handoff docs first.

## Safety Boundaries

- Do not commit real `.env` files or secrets.
- Do not integrate payment, SMS, email, third-party login, paid OCR, or real AI providers during early phases unless the docs explicitly change.
- Do not provide medical, legal, or investment judgments.
- Do not weaken business rules just to make tests pass.
- Do not overwrite user changes in a dirty worktree.

## Final Response Shape

When ending a development turn, include:

- What was completed.
- Files changed.
- Verification run and whether it passed.
- Remaining issues or blockers.
- Whether a stop condition was triggered.
- Next automatic task if continuing is allowed.
- Suggested commit message.
