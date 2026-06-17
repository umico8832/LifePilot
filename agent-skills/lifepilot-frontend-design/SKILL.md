---
name: lifepilot-frontend-design
description: LifePilot-specific frontend design guidance for building or reshaping Vue 3 screens, dashboards, forms, CRUD pages, navigation, empty states, and responsive layouts. Use when implementing user-facing UI in `frontend/src/**`, improving visual polish, designing LifePilot product surfaces, or reviewing UI that risks looking generic, cluttered, or inconsistent with the app's life-management workspace.
---

# LifePilot Frontend Design

Design LifePilot as a calm, practical life operations workspace: useful every day, easy to scan, and warm enough for personal/family data without becoming decorative.

Adapted from Anthropic's `frontend-design` skill ideas and modified for LifePilot's Vue 3 + Element Plus stack. See `LICENSE.txt`.

## Workflow

1. Read the relevant product docs before designing: `docs/PRD.md`, `docs/PRODUCT_STRATEGY.md`, and the module-specific API/database docs when the screen touches backend data.
2. Inspect existing frontend conventions in `frontend/src/App.vue`, `frontend/src/styles.css`, `frontend/src/views`, `frontend/src/layouts`, `frontend/src/components`, `frontend/src/stores`, and `frontend/src/api`.
3. Define the screen's job in one sentence: what decision or action should become easier?
4. Create a compact design direction before editing: palette, information hierarchy, layout, interaction states, and the one distinctive LifePilot detail.
5. Implement with Vue 3, TypeScript, Pinia, Vue Router, Axios, Element Plus, and ECharts only when useful.
6. Build and visually verify responsive behavior. Use `lifepilot-webapp-testing` for browser checks when changing meaningful UI.

## Visual Direction

- Prefer an operations-console feel over a marketing landing page. First screens should expose the actual product workflow.
- Use restrained color with functional contrast. Avoid one-note blue/slate, beige, purple gradients, and decorative orbs.
- Spend distinctiveness on one grounded detail per surface: timeline rhythm for life logs, pantry/location semantics for inventory, household/member signals for spaces, or ledger/category structure for finance.
- Keep page sections unframed. Use cards only for repeated items, modals, compact summaries, and genuinely framed tools.
- Match type scale to context. Dashboards and forms need dense, legible headings, not hero-scale text.
- Use Element Plus controls where they fit, but tune spacing, grouping, empty states, and helper copy so the result does not feel like a raw component demo.
- Use lucide or existing icon libraries if already installed; otherwise avoid adding an icon package just for decoration.

## Product UI Rules

- Never show fake business data as if it were real. If data is placeholder-only, label it as empty or mock in dev-facing context.
- Every CRUD view should include loading, empty, error, success, and permission/unauthorized states when relevant.
- Every space-scoped view must make the current space visible enough that users understand where data is being written.
- Actions should use stable verbs: "Save changes", "Create space", "Add expense", "Mark purchased". Keep the same action name in buttons, dialogs, and toasts.
- Errors should say what happened and what the user can do next. Avoid vague apologies.
- Mobile layouts must preserve task completion. Do not hide primary actions behind obscure controls.

## Implementation Notes

- Prefer route-level screens in `frontend/src/views`, API clients in `frontend/src/api`, stores in `frontend/src/stores`, and shared UI in `frontend/src/components`.
- Keep CSS responsive with explicit dimensions, grid tracks, `minmax`, `aspect-ratio`, and stable control sizes to avoid layout shifts.
- Do not scale font size with viewport width.
- Avoid adding new UI frameworks, global CSS resets, or visual systems without a project decision.

## Verification

Run:

```bash
cd frontend && npm run build
```

For local visual checks, use the current project convention:

```bash
cd frontend && BACKEND_PROXY_TARGET=http://localhost:18081 npm run dev
```

Then verify the page in a browser at desktop and mobile widths, checking console errors, overflowing text, and basic interaction flow.
