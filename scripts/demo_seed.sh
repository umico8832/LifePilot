#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SQL_FILE="$ROOT_DIR/scripts/demo_seed.sql"

MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_DATABASE="${MYSQL_DATABASE:-lifepilot}"
MYSQL_USER="${MYSQL_USER:-lifepilot}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-lifepilot_dev_password}"

usage() {
  cat <<USAGE
Usage: scripts/demo_seed.sh [--dry-run|--apply|--verify]

Options:
  --dry-run  Validate that the SQL file exists and print the target database.
  --apply    Reset and recreate the local demo account and demo data.
  --verify   Count demo records in the target database.

Environment:
  MYSQL_HOST      default 127.0.0.1
  MYSQL_PORT      default 3306
  MYSQL_DATABASE  default lifepilot
  MYSQL_USER      default lifepilot
  MYSQL_PASSWORD  default lifepilot_dev_password

Demo login after --apply:
  email:    demo@lifepilot.local
  password: demo-pass-123
USAGE
}

mode="${1:---dry-run}"

if [[ "$mode" == "-h" || "$mode" == "--help" ]]; then
  usage
  exit 0
fi

if [[ ! -f "$SQL_FILE" ]]; then
  echo "SQL file not found: $SQL_FILE" >&2
  exit 1
fi

if [[ "$mode" == "--dry-run" ]]; then
  echo "Demo seed dry-run"
  echo "SQL: $SQL_FILE"
  echo "Target: $MYSQL_USER@$MYSQL_HOST:$MYSQL_PORT/$MYSQL_DATABASE"
  echo "No database changes were made. Run with --apply to seed demo data."
  exit 0
fi

require_mysql() {
  if ! command -v mysql >/dev/null 2>&1; then
    echo "mysql client is required. Install it or run inside an environment that provides mysql." >&2
    exit 1
  fi
}

mysql_cmd() {
  MYSQL_PWD="$MYSQL_PASSWORD" mysql \
    --host="$MYSQL_HOST" \
    --port="$MYSQL_PORT" \
    --user="$MYSQL_USER" \
    --database="$MYSQL_DATABASE" \
    --default-character-set=utf8mb4 \
    "$@"
}

if [[ "$mode" == "--apply" ]]; then
  require_mysql
  echo "Applying demo seed to $MYSQL_USER@$MYSQL_HOST:$MYSQL_PORT/$MYSQL_DATABASE"
  mysql_cmd < "$SQL_FILE"
  echo "Demo seed complete."
  exit 0
fi

if [[ "$mode" == "--verify" ]]; then
  require_mysql
  mysql_cmd --table <<'SQL'
SELECT
    u.email AS demo_email,
    h.name AS demo_space,
    (SELECT COUNT(*) FROM transaction_record tr WHERE tr.household_id = h.id) AS transactions,
    (SELECT COUNT(*) FROM shopping_list sl WHERE sl.household_id = h.id) AS shopping_lists,
    (SELECT COUNT(*) FROM inventory_item ii WHERE ii.household_id = h.id) AS inventory_items,
    (SELECT COUNT(*) FROM todo_task tt WHERE tt.household_id = h.id) AS todo_tasks,
    (SELECT COUNT(*) FROM recipe r WHERE r.household_id = h.id) AS recipes,
    (SELECT COUNT(*) FROM meal_plan mp WHERE mp.household_id = h.id) AS meal_plans,
    (SELECT COUNT(*) FROM ai_call_log al WHERE al.household_id = h.id) AS ai_logs
FROM users u
JOIN household h ON h.owner_user_id = u.id
WHERE u.email = 'demo@lifepilot.local'
ORDER BY h.id DESC
LIMIT 1;
SQL
  exit 0
fi

echo "Unknown option: $mode" >&2
usage >&2
exit 1
