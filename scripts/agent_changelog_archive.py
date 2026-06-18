#!/usr/bin/env python3
"""Archive old agent changelog entries and refresh recent history."""

from __future__ import annotations

import argparse
import re
from collections import defaultdict
from datetime import datetime
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
CHANGELOG = ROOT / "docs" / "CHANGELOG_AGENT.md"
RECENT_HISTORY = ROOT / "docs" / "RECENT_HISTORY.md"
ARCHIVE_DIR = ROOT / "docs" / "changelog"

ENTRY_RE = re.compile(r"^## (\d{4})-(\d{2})-\d{2}[^\n]*$", re.M)


def split_entries(text: str) -> tuple[str, list[str]]:
    matches = list(ENTRY_RE.finditer(text))
    if not matches:
        return text.rstrip() + "\n", []

    header = text[: matches[0].start()].rstrip() + "\n"
    entries: list[str] = []
    for index, match in enumerate(matches):
        start = match.start()
        end = matches[index + 1].start() if index + 1 < len(matches) else len(text)
        entries.append(text[start:end].strip() + "\n")
    return header, entries


def entry_month(entry: str) -> str:
    match = ENTRY_RE.search(entry)
    if not match:
        raise ValueError("Entry does not start with a dated heading.")
    return f"{match.group(1)}-{match.group(2)}"


def entry_key(entry: str) -> str:
    return entry.splitlines()[0].strip()


def entry_datetime(entry: str) -> datetime:
    heading = entry_key(entry).replace("## ", "", 1).strip()
    timestamp = heading[:16]
    try:
        return datetime.strptime(timestamp, "%Y-%m-%d %H:%M")
    except ValueError as exc:
        raise ValueError(f"Entry heading does not contain a supported timestamp: {heading}") from exc


def sort_entries_newest_first(entries: list[str]) -> list[str]:
    return sorted(entries, key=entry_datetime, reverse=True)


def refresh_changelog(header: str, kept_entries: list[str], keep: int) -> str:
    intro = (
        "# Agent Changelog\n\n"
        "本文件只保留最近若干条完整开发记录；更早历史由 "
        "`scripts/agent_changelog_archive.py` 自动归档到 `docs/changelog/`。\n\n"
        "默认接手请读 `docs/RECENT_HISTORY.md`。需要追溯具体历史时，再按月份查看归档文件。\n\n"
        "## 维护方式\n\n"
        "```bash\n"
        f"python3 scripts/agent_changelog_archive.py --keep {keep}\n"
        "```\n\n"
        f"脚本默认保留最近 {keep} 条完整记录，并刷新 `docs/RECENT_HISTORY.md`。\n\n"
    )

    old_extra = header.replace("# Agent Changelog", "", 1).strip()
    if old_extra and "scripts/agent_changelog_archive.py" not in old_extra:
        intro += old_extra + "\n\n"

    return intro + "\n\n".join(entry.rstrip() for entry in kept_entries).rstrip() + "\n"


def extract_bullet(entry: str, label: str) -> str:
    match = re.search(rf"^- {re.escape(label)}：(.+)$", entry, re.M)
    return match.group(1).strip() if match else ""


def clean_summary(value: str) -> str:
    return value.rstrip("。.")


def refresh_recent_history(entries: list[str], recent_keep: int) -> str:
    lines = [
        "# Recent History",
        "",
        "本文件是新 Agent 默认阅读的短历史摘要，只保留最近任务脉络。",
        "完整历史请查 `docs/CHANGELOG_AGENT.md` 和 `docs/changelog/` 归档。",
        "",
        "## 最近完成",
        "",
    ]

    for entry in entries[:recent_keep]:
        heading = entry.splitlines()[0].replace("## ", "", 1)
        task = clean_summary(extract_bullet(entry, "Agent 任务名称")) or "未标注任务"
        verification = clean_summary(extract_bullet(entry, "测试结果")) or "未记录验证"
        next_task = clean_summary(extract_bullet(entry, "下一步任务")) or "未记录下一步"
        lines.append(f"- {heading}：{task}；验证：{verification}；下一步：{next_task}")

    lines.extend(
        [
            "",
            "## 维护规则",
            "",
            "- 本文件由 `scripts/agent_changelog_archive.py` 刷新。",
            "- 不在这里记录当前状态；当前状态只看 `docs/CURRENT_STATE.md`。",
            "- 不在这里记录完整历史；完整历史按需查 `docs/CHANGELOG_AGENT.md` 和 `docs/changelog/`。",
            "",
        ]
    )
    return "\n".join(lines)


def read_archive_entries(path: Path) -> tuple[str, list[str]]:
    if not path.exists():
        return "", []
    return split_entries(path.read_text(encoding="utf-8"))


def archive_entries(entries: list[str]) -> None:
    ARCHIVE_DIR.mkdir(parents=True, exist_ok=True)
    by_month: dict[str, list[str]] = defaultdict(list)
    for entry in entries:
        by_month[entry_month(entry)].append(entry)

    for month, month_entries in sorted(by_month.items()):
        archive_path = ARCHIVE_DIR / f"{month}.md"
        _, existing_entries = read_archive_entries(archive_path)
        combined: dict[str, str] = {entry_key(entry): entry for entry in existing_entries}
        for entry in month_entries:
            combined.setdefault(entry_key(entry), entry)
        sorted_entries = sort_entries_newest_first(list(combined.values()))

        archive_text = (
            f"# Agent Changelog Archive: {month}\n\n"
            "本文件由 `scripts/agent_changelog_archive.py` 自动维护。"
            "默认接手不要全文读取，按需追溯具体历史。"
            "条目按时间倒序排列。\n\n"
            + "\n\n".join(entry.rstrip() for entry in sorted_entries).rstrip()
            + "\n"
        )
        archive_path.write_text(archive_text, encoding="utf-8")


def normalize_archives() -> None:
    if not ARCHIVE_DIR.exists():
        return

    for archive_path in sorted(ARCHIVE_DIR.glob("*.md")):
        month = archive_path.stem
        _, existing_entries = read_archive_entries(archive_path)
        if not existing_entries:
            continue
        sorted_entries = sort_entries_newest_first(existing_entries)
        archive_text = (
            f"# Agent Changelog Archive: {month}\n\n"
            "本文件由 `scripts/agent_changelog_archive.py` 自动维护。"
            "默认接手不要全文读取，按需追溯具体历史。"
            "条目按时间倒序排列。\n\n"
            + "\n\n".join(entry.rstrip() for entry in sorted_entries).rstrip()
            + "\n"
        )
        archive_path.write_text(archive_text, encoding="utf-8")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--keep", type=int, default=10, help="full entries to keep in CHANGELOG_AGENT.md")
    parser.add_argument("--recent-keep", type=int, default=5, help="summary entries to keep in RECENT_HISTORY.md")
    args = parser.parse_args()

    if args.keep < 1:
        raise SystemExit("--keep must be positive")
    if args.recent_keep < 1:
        raise SystemExit("--recent-keep must be positive")

    header, entries = split_entries(CHANGELOG.read_text(encoding="utf-8"))
    sorted_entries = sort_entries_newest_first(entries)
    kept_entries = sorted_entries[: args.keep]
    archived_entries = sorted_entries[args.keep :]

    if archived_entries:
        archive_entries(archived_entries)
    normalize_archives()

    CHANGELOG.write_text(refresh_changelog(header, kept_entries, args.keep), encoding="utf-8")
    RECENT_HISTORY.write_text(refresh_recent_history(sorted_entries, args.recent_keep), encoding="utf-8")

    print(f"[OK] Kept {len(kept_entries)} changelog entries.")
    print(f"[OK] Archived {len(archived_entries)} changelog entries.")
    print(f"[OK] Refreshed docs/RECENT_HISTORY.md with {min(len(entries), args.recent_keep)} entries.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
