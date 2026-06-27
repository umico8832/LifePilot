# Database Design

## 数据库规范

- 数据库：MySQL 8。
- 字符集：`utf8mb4`。
- 主键：优先使用 `BIGINT` 雪花或数据库自增，后续统一。
- 时间字段：`created_at`、`updated_at`、可选 `deleted_at`。
- 软删除：业务表可使用 `deleted_at` 或 `is_deleted`。
- 多租户隔离：生活数据必须包含 `space_id` 或等价归属字段。

## ER 关系

- `user` 1:N `household_member`
- `household` 1:N `household_member`
- `household` 1:N `transaction_record`
- `household` 1:N `shopping_list`
- `shopping_list` 1:N `shopping_item`
- `household` 1:N `inventory_item`
- `household` 1:N `todo_task`
- `household` 1:N `document_record`
- `household` 1:N `meal_plan`
- `recipe` 1:N `meal_plan`
- `user` 1:N `ai_call_log`

## 核心表规划

### user / users

逻辑模型名为 `user`，当前物理表名为 `users`，避免数据库系统用户概念和潜在保留字冲突。

- `id`
- `email`
- `password_hash`
- `display_name`
- `avatar_url`
- `status`
- `created_at`
- `updated_at`

### household / space

- `id`
- `name`
- `type`：personal 或 family
- `owner_user_id`
- `created_at`
- `updated_at`

### household_member

- `id`
- `household_id`
- `user_id`
- `role`：owner、admin、member、viewer
- `status`
- `created_at`
- `updated_at`

### transaction_record

- `id`
- `household_id`
- `user_id`
- `category_id`
- `type`：income 或 expense
- `amount`
- `currency`
- `occurred_at`
- `merchant`
- `note`
- `source`：manual 或 ai
- `created_at`
- `updated_at`

### transaction_category

- `id`
- `household_id`
- `name`
- `type`
- `icon`
- `color`
- `created_at`
- `updated_at`

### budget

- `id`
- `household_id`
- `category_id`
- `period_type`
- `period_start`
- `amount`
- `created_at`
- `updated_at`

### shopping_list

- `id`
- `household_id`
- `name`
- `status`
- `estimated_budget`
- `created_by`
- `created_at`
- `updated_at`

### shopping_item

- `id`
- `shopping_list_id`
- `name`
- `quantity`
- `unit`
- `estimated_price`
- `purchased`
- `inventory_item_id`
- `created_at`
- `updated_at`

### inventory_item

- `id`
- `household_id`
- `name`
- `category`
- `quantity`
- `unit`
- `location`
- `expire_at`
- `low_stock_threshold`
- `created_at`
- `updated_at`

### recipe

- `id`
- `household_id`
- `name`
- `description`
- `ingredients_json`
- `steps_json`
- `created_by`
- `created_at`
- `updated_at`

### todo_task

- `id`
- `household_id`
- `title`
- `description`
- `status`
- `priority`
- `due_at`
- `repeat_rule`
- `assigned_to`
- `created_at`
- `updated_at`

### document_record

- `id`
- `household_id`
- `title`
- `type`
- `issuer`
- `document_date`
- `expire_at`
- `storage_location`
- `metadata_json`
- `created_at`
- `updated_at`

### meal_plan

- `id`
- `household_id`
- `recipe_id`
- `planned_date`
- `meal_type`：breakfast、lunch、dinner、snack
- `note`
- `created_by`
- `created_at`
- `updated_at`

### ai_call_log

- `id`
- `user_id`
- `household_id`
- `provider`
- `scenario`
- `prompt_hash`
- `request_json`
- `response_json`
- `status`
- `duration_ms`
- `error_message`
- `created_at`
- `updated_at`
