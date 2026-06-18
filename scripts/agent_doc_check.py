#!/usr/bin/env python3
"""Check LifePilot agent documentation consistency."""

from __future__ import annotations

import re
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]


def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")


def fail(message: str) -> None:
    print(f"[FAIL] {message}")
    raise SystemExit(1)


def ok(message: str) -> None:
    print(f"[OK] {message}")


def extract_section(text: str, heading: str) -> str:
    marker = f"## {heading}"
    start = text.find(marker)
    if start == -1:
        fail(f"Missing section: {heading}")
    rest = text[start + len(marker) :]
    next_heading = rest.find("\n## ")
    return rest if next_heading == -1 else rest[:next_heading]


def parse_backlog_tasks(backlog: str) -> dict[str, dict[str, str]]:
    tasks: dict[str, dict[str, str]] = {}
    matches = list(re.finditer(r"^### (P\d+-\d+) (.+)$", backlog, re.M))
    for index, match in enumerate(matches):
        task_id = match.group(1)
        title = match.group(2).strip()
        start = match.end()
        end = matches[index + 1].start() if index + 1 < len(matches) else len(backlog)
        body = backlog[start:end]
        status_match = re.search(r"- 状态：([a-z_]+)", body)
        tasks[task_id] = {
            "title": title,
            "status": status_match.group(1) if status_match else "",
        }
    return tasks


def main() -> int:
    current = read("docs/CURRENT_STATE.md")
    backlog = read("docs/BACKLOG.md")
    changelog = read("docs/CHANGELOG_AGENT.md")
    recent_history = read("docs/RECENT_HISTORY.md")
    handoff = read("docs/HANDOFF.md")
    next_prompt = read("docs/NEXT_CHAT_PROMPT.md")

    tasks = parse_backlog_tasks(backlog)
    todo_tasks = [task_id for task_id, meta in tasks.items() if meta["status"] == "todo"]
    if not todo_tasks:
        fail("BACKLOG has no todo tasks. This triggers the autonomous-development stop condition.")
    ok(f"BACKLOG has todo tasks: {', '.join(todo_tasks[:5])}")

    next_section = extract_section(current, "下一项自动任务")
    next_match = re.search(r"(P\d+-\d+)", next_section)
    if not next_match:
        fail("CURRENT_STATE next task does not contain a task id.")
    next_task = next_match.group(1)
    if next_task not in tasks:
        fail(f"CURRENT_STATE next task {next_task} is not present in BACKLOG.")
    if tasks[next_task]["status"] != "todo":
        fail(f"CURRENT_STATE next task {next_task} is not todo in BACKLOG.")
    ok(f"CURRENT_STATE next task {next_task} matches BACKLOG todo task.")

    current_stage = extract_section(current, "当前阶段")
    completed_ids = re.findall(r"(P\d+-\d+)", current_stage)
    recent_completed = completed_ids[-1] if completed_ids else None
    if recent_completed and recent_completed not in changelog:
        archive_text = "\n".join(
            path.read_text(encoding="utf-8")
            for path in sorted((ROOT / "docs" / "changelog").glob("*.md"))
        )
        if recent_completed not in archive_text:
            fail(f"CHANGELOG_AGENT and archives do not mention recent completed task {recent_completed}.")
    if recent_completed:
        ok(f"CHANGELOG_AGENT or archive mentions recent completed task {recent_completed}.")
        if recent_completed not in recent_history:
            fail(f"RECENT_HISTORY does not mention recent completed task {recent_completed}.")
        ok(f"RECENT_HISTORY mentions recent completed task {recent_completed}.")

    stale_patterns = [
        r"^## 当前阶段",
        r"^## 下一项自动任务",
        r"^## 下一步任务",
        r"^## 下一步建议任务",
        r"P\d+-\d+",
    ]
    for path, text in [
        ("docs/HANDOFF.md", handoff),
        ("docs/NEXT_CHAT_PROMPT.md", next_prompt),
    ]:
        for pattern in stale_patterns:
            if re.search(pattern, text, re.M):
                fail(f"{path} contains state-like pattern `{pattern}`; keep state in CURRENT_STATE/BACKLOG.")
        ok(f"{path} does not duplicate current task state.")

    required_handoff_refs = [
        "docs/CURRENT_STATE.md",
        "docs/BACKLOG.md",
        "docs/RECENT_HISTORY.md",
    ]
    for ref in required_handoff_refs:
        if ref not in handoff:
            fail(f"docs/HANDOFF.md does not reference {ref}.")
    ok("HANDOFF references authoritative docs.")

    if "docs/changelog/" not in changelog:
        fail("CHANGELOG_AGENT does not explain archived history in docs/changelog/.")
    ok("CHANGELOG_AGENT references archive directory.")

    print("[OK] Agent documentation consistency check passed.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
