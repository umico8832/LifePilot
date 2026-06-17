---
name: lifepilot-webapp-testing
description: LifePilot-specific local web application testing workflow for verifying Vue screens, authentication flows, backend/frontend integration, browser console health, responsive layout, and user interactions. Use after meaningful changes to `frontend/src/**`, API integration, routing, auth, dashboards, forms, or any UI behavior that should be proven in a browser.
---

# LifePilot Webapp Testing

Verify LifePilot UI changes with a running backend/frontend pair and a real browser automation pass when the behavior is user-facing.

Adapted from Anthropic's `webapp-testing` skill and modified for LifePilot's ports and stack. See `LICENSE.txt`.

## Project Defaults

- Backend default port: `8080`
- Local fallback backend port: `18081`
- MySQL fallback port: `3307`
- Frontend dev port: `5173`
- Frontend proxy env: `BACKEND_PROXY_TARGET=http://localhost:18081`

Use the fallback ports when `8080` or `3306` are occupied, as documented in `docs/HANDOFF.md`.

## Workflow

1. Run static verification first:

```bash
cd backend && mvn test
cd frontend && npm run build
```

2. Start services if the flow needs real integration:

```bash
MYSQL_PORT=3307 docker compose up -d mysql
cd backend && BACKEND_PORT=18081 SPRING_DATASOURCE_URL='jdbc:mysql://localhost:3307/lifepilot?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC' mvn spring-boot:run
cd frontend && BACKEND_PROXY_TARGET=http://localhost:18081 npm run dev
```

3. Open `http://localhost:5173` and verify:

- The relevant screen renders without console errors.
- Loading, empty, error, and success states behave as expected.
- Auth-required routes handle missing/invalid tokens correctly.
- Text does not overflow on desktop or mobile widths.
- Primary form submissions and navigation paths complete against real backend endpoints when available.

## Playwright Helper

Use `scripts/with_server.py` when you need a repeatable local automation run. Run `--help` before using it:

```bash
python3 agent-skills/lifepilot-webapp-testing/scripts/with_server.py --help
```

Example shape:

```bash
python3 agent-skills/lifepilot-webapp-testing/scripts/with_server.py \
  --server "cd backend && BACKEND_PORT=18081 SPRING_DATASOURCE_URL='jdbc:mysql://localhost:3307/lifepilot?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC' mvn spring-boot:run" --port 18081 \
  --server "cd frontend && BACKEND_PROXY_TARGET=http://localhost:18081 npm run dev" --port 5173 \
  -- python3 /tmp/lifepilot-ui-check.py
```

Automation scripts should contain only browser logic:

```python
from playwright.sync_api import sync_playwright

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    page = browser.new_page(viewport={"width": 1366, "height": 900})
    errors = []
    page.on("console", lambda msg: errors.append(msg.text) if msg.type == "error" else None)
    page.goto("http://localhost:5173")
    page.wait_for_load_state("networkidle")
    assert not errors, errors
    browser.close()
```

## Browser Check Pattern

1. Navigate and wait for `networkidle`.
2. Capture console errors before interacting.
3. Inspect rendered DOM or screenshot if selectors are unclear.
4. Interact using visible text, roles, labels, or stable selectors.
5. Repeat at a mobile viewport such as `390x844`.
6. Record exactly what was verified in `docs/CHANGELOG_AGENT.md` when the task changes project state.

## When Tests Cannot Run

Do not claim success. Record the reason in the final response and, for completed project tasks, in `docs/CHANGELOG_AGENT.md`.
