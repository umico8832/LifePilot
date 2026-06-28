-- LifePilot local demo seed data.
-- This script resets only data owned by demo@lifepilot.local, then creates
-- a deterministic demo space with finance, shopping, inventory, todo, recipe,
-- meal plan, document, and AI call log data.

START TRANSACTION;

SET @demo_email = 'demo@lifepilot.local';
SET @demo_password_note = 'demo-pass-123';
SET @demo_user_id = (SELECT id FROM users WHERE email = @demo_email LIMIT 1);

DELETE FROM ai_call_log
WHERE user_id = @demo_user_id
   OR household_id IN (SELECT id FROM household WHERE owner_user_id = @demo_user_id);

DELETE FROM meal_plan
WHERE household_id IN (SELECT id FROM household WHERE owner_user_id = @demo_user_id);

DELETE FROM document_record
WHERE household_id IN (SELECT id FROM household WHERE owner_user_id = @demo_user_id);

DELETE FROM todo_task
WHERE household_id IN (SELECT id FROM household WHERE owner_user_id = @demo_user_id);

DELETE si FROM shopping_item si
JOIN shopping_list sl ON sl.id = si.shopping_list_id
WHERE sl.household_id IN (SELECT id FROM household WHERE owner_user_id = @demo_user_id);

DELETE FROM shopping_list
WHERE household_id IN (SELECT id FROM household WHERE owner_user_id = @demo_user_id);

DELETE FROM inventory_item
WHERE household_id IN (SELECT id FROM household WHERE owner_user_id = @demo_user_id);

DELETE FROM transaction_record
WHERE household_id IN (SELECT id FROM household WHERE owner_user_id = @demo_user_id);

DELETE FROM transaction_category
WHERE household_id IN (SELECT id FROM household WHERE owner_user_id = @demo_user_id);

DELETE FROM recipe
WHERE household_id IN (SELECT id FROM household WHERE owner_user_id = @demo_user_id);

DELETE FROM household_member
WHERE household_id IN (SELECT id FROM household WHERE owner_user_id = @demo_user_id)
   OR user_id = @demo_user_id;

DELETE FROM household WHERE owner_user_id = @demo_user_id;
DELETE FROM users WHERE id = @demo_user_id;

INSERT INTO users (email, password_hash, display_name, avatar_url, status, created_at, updated_at)
VALUES (
    @demo_email,
    '$2a$10$SzhoxoQHvTB9GQ3c0vCj1.TfzGi5W2.5XlbplZTGN6FPw2Qth75Ti',
    'LifePilot Demo',
    NULL,
    'active',
    NOW(6),
    NOW(6)
);
SET @user_id = LAST_INSERT_ID();

INSERT INTO household (name, type, owner_user_id, created_at, updated_at)
VALUES ('LifePilot Demo Home', 'family', @user_id, NOW(6), NOW(6));
SET @space_id = LAST_INSERT_ID();

INSERT INTO household_member (household_id, user_id, role, status, created_at, updated_at)
VALUES (@space_id, @user_id, 'owner', 'active', NOW(6), NOW(6));

INSERT INTO transaction_category (household_id, name, type, icon, color, created_at, updated_at) VALUES
(@space_id, '餐饮', 'expense', 'utensils', '#ef4444', NOW(6), NOW(6)),
(@space_id, '日用品', 'expense', 'shopping-bag', '#f59e0b', NOW(6), NOW(6)),
(@space_id, '交通', 'expense', 'car', '#3b82f6', NOW(6), NOW(6)),
(@space_id, '工资', 'income', 'wallet', '#16a34a', NOW(6), NOW(6)),
(@space_id, '副业', 'income', 'briefcase', '#14b8a6', NOW(6), NOW(6));

SET @cat_food = (SELECT id FROM transaction_category WHERE household_id = @space_id AND name = '餐饮' LIMIT 1);
SET @cat_daily = (SELECT id FROM transaction_category WHERE household_id = @space_id AND name = '日用品' LIMIT 1);
SET @cat_transport = (SELECT id FROM transaction_category WHERE household_id = @space_id AND name = '交通' LIMIT 1);
SET @cat_salary = (SELECT id FROM transaction_category WHERE household_id = @space_id AND name = '工资' LIMIT 1);
SET @cat_side = (SELECT id FROM transaction_category WHERE household_id = @space_id AND name = '副业' LIMIT 1);

INSERT INTO transaction_record
(household_id, user_id, category_id, type, amount, currency, occurred_at, merchant, note, source, created_at, updated_at)
VALUES
(@space_id, @user_id, @cat_salary, 'income', 18500.00, 'CNY', DATE_ADD(CURDATE(), INTERVAL -10 DAY), 'Demo Company', '本月工资', 'manual', NOW(6), NOW(6)),
(@space_id, @user_id, @cat_side, 'income', 2200.00, 'CNY', DATE_ADD(CURDATE(), INTERVAL -4 DAY), 'Demo Studio', '周末咨询项目', 'manual', NOW(6), NOW(6)),
(@space_id, @user_id, @cat_food, 'expense', 86.50, 'CNY', DATE_ADD(CURDATE(), INTERVAL -8 DAY), '社区超市', '晚餐食材', 'manual', NOW(6), NOW(6)),
(@space_id, @user_id, @cat_food, 'expense', 128.00, 'CNY', DATE_ADD(CURDATE(), INTERVAL -5 DAY), '家庭餐厅', '周末聚餐', 'ai', NOW(6), NOW(6)),
(@space_id, @user_id, @cat_daily, 'expense', 246.80, 'CNY', DATE_ADD(CURDATE(), INTERVAL -3 DAY), '家居便利店', '清洁用品和纸巾', 'manual', NOW(6), NOW(6)),
(@space_id, @user_id, @cat_transport, 'expense', 42.00, 'CNY', DATE_ADD(CURDATE(), INTERVAL -1 DAY), '地铁', '通勤充值', 'manual', NOW(6), NOW(6));

INSERT INTO shopping_list (household_id, name, status, estimated_budget, created_by, created_at, updated_at)
VALUES
(@space_id, '本周家庭采购', 'active', 420.00, @user_id, DATE_ADD(NOW(6), INTERVAL -2 DAY), NOW(6)),
(@space_id, '已完成的早餐补货', 'completed', 160.00, @user_id, DATE_ADD(NOW(6), INTERVAL -12 DAY), DATE_ADD(NOW(6), INTERVAL -11 DAY));

SET @shopping_active = (SELECT id FROM shopping_list WHERE household_id = @space_id AND name = '本周家庭采购' LIMIT 1);
SET @shopping_done = (SELECT id FROM shopping_list WHERE household_id = @space_id AND name = '已完成的早餐补货' LIMIT 1);

INSERT INTO shopping_item (shopping_list_id, name, quantity, unit, estimated_price, purchased, inventory_item_id, created_at, updated_at)
VALUES
(@shopping_active, '牛奶', 2, '盒', 36.00, 0, NULL, NOW(6), NOW(6)),
(@shopping_active, '鸡胸肉', 1.5, 'kg', 58.00, 1, NULL, NOW(6), NOW(6)),
(@shopping_active, '西兰花', 3, '颗', 24.00, 0, NULL, NOW(6), NOW(6)),
(@shopping_active, '洗衣液', 1, '瓶', 49.90, 0, NULL, NOW(6), NOW(6)),
(@shopping_done, '燕麦', 2, '袋', 39.80, 1, NULL, NOW(6), NOW(6)),
(@shopping_done, '鸡蛋', 30, '枚', 45.00, 1, NULL, NOW(6), NOW(6));

INSERT INTO inventory_item
(household_id, name, category, quantity, unit, location, expire_at, low_stock_threshold, created_at, updated_at)
VALUES
(@space_id, '牛奶', '冷藏', 1, '盒', '冰箱上层', DATE_ADD(NOW(6), INTERVAL 3 DAY), 2, NOW(6), NOW(6)),
(@space_id, '鸡蛋', '冷藏', 6, '枚', '冰箱蛋架', DATE_ADD(NOW(6), INTERVAL 9 DAY), 8, NOW(6), NOW(6)),
(@space_id, '米', '主食', 4.5, 'kg', '厨房储物柜', DATE_ADD(NOW(6), INTERVAL 120 DAY), 2, NOW(6), NOW(6)),
(@space_id, '咖啡豆', '饮品', 0.2, 'kg', '吧台', DATE_ADD(NOW(6), INTERVAL 45 DAY), 0.5, NOW(6), NOW(6)),
(@space_id, '洗衣液', '日用品', 0.5, '瓶', '阳台柜', NULL, 1, NOW(6), NOW(6)),
(@space_id, '西红柿', '蔬菜', 5, '个', '冰箱抽屉', DATE_ADD(NOW(6), INTERVAL 2 DAY), 2, NOW(6), NOW(6));

INSERT INTO todo_task
(household_id, title, description, status, priority, due_at, repeat_rule, assigned_to, created_at, updated_at)
VALUES
(@space_id, '缴纳水电费', '演示：逾期待办会出现在首页统计中', 'pending', 'urgent', DATE_ADD(NOW(6), INTERVAL -1 DAY), NULL, @user_id, DATE_ADD(NOW(6), INTERVAL -6 DAY), DATE_ADD(NOW(6), INTERVAL -6 DAY)),
(@space_id, '预约空调清洗', '联系附近维修师傅', 'in_progress', 'medium', DATE_ADD(NOW(6), INTERVAL 4 DAY), NULL, @user_id, DATE_ADD(NOW(6), INTERVAL -3 DAY), DATE_ADD(NOW(6), INTERVAL -1 DAY)),
(@space_id, '整理冰箱库存', '把临期食材加入本周菜单', 'completed', 'high', DATE_ADD(NOW(6), INTERVAL -2 DAY), NULL, @user_id, DATE_ADD(NOW(6), INTERVAL -8 DAY), DATE_ADD(NOW(6), INTERVAL -2 DAY)),
(@space_id, '周末采购复盘', '检查购物预算是否超支', 'completed', 'low', DATE_ADD(NOW(6), INTERVAL -10 DAY), NULL, @user_id, DATE_ADD(NOW(6), INTERVAL -13 DAY), DATE_ADD(NOW(6), INTERVAL -10 DAY)),
(@space_id, '旧任务示例', '取消的事项不会计入完成率分母', 'cancelled', 'low', NULL, NULL, @user_id, DATE_ADD(NOW(6), INTERVAL -15 DAY), DATE_ADD(NOW(6), INTERVAL -14 DAY));

INSERT INTO recipe
(household_id, name, description, ingredients_json, steps_json, created_by, created_at, updated_at)
VALUES
(@space_id, '番茄鸡蛋面', '适合工作日晚餐的快速菜谱',
 '[{"name":"西红柿","quantity":2,"unit":"个"},{"name":"鸡蛋","quantity":2,"unit":"枚"},{"name":"面条","quantity":200,"unit":"g"}]',
 '["西红柿切块，鸡蛋打散","炒香番茄后加水煮开","下面条，出锅前淋入蛋液"]',
 @user_id, NOW(6), NOW(6)),
(@space_id, '西兰花鸡胸便当', '高蛋白午餐示例',
 '[{"name":"鸡胸肉","quantity":300,"unit":"g"},{"name":"西兰花","quantity":1,"unit":"颗"},{"name":"米","quantity":150,"unit":"g"}]',
 '["鸡胸肉腌制后煎熟","西兰花焯水","装入便当盒"]',
 @user_id, NOW(6), NOW(6)),
(@space_id, '燕麦牛奶早餐', '低维护早餐',
 '[{"name":"燕麦","quantity":50,"unit":"g"},{"name":"牛奶","quantity":1,"unit":"盒"},{"name":"咖啡豆","quantity":15,"unit":"g"}]',
 '["燕麦加入牛奶浸泡","冲一杯咖啡","搭配水果"]',
 @user_id, NOW(6), NOW(6));

SET @recipe_noodle = (SELECT id FROM recipe WHERE household_id = @space_id AND name = '番茄鸡蛋面' LIMIT 1);
SET @recipe_bento = (SELECT id FROM recipe WHERE household_id = @space_id AND name = '西兰花鸡胸便当' LIMIT 1);
SET @recipe_oat = (SELECT id FROM recipe WHERE household_id = @space_id AND name = '燕麦牛奶早餐' LIMIT 1);

INSERT INTO meal_plan
(household_id, recipe_id, planned_date, meal_type, note, created_by, created_at, updated_at)
VALUES
(@space_id, @recipe_oat, CURDATE(), 'breakfast', '演示早餐计划', @user_id, NOW(6), NOW(6)),
(@space_id, @recipe_bento, CURDATE(), 'lunch', '午餐便当', @user_id, NOW(6), NOW(6)),
(@space_id, @recipe_noodle, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'dinner', '下班后 20 分钟完成', @user_id, NOW(6), NOW(6)),
(@space_id, @recipe_bento, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'lunch', '复用鸡胸肉库存', @user_id, NOW(6), NOW(6));

INSERT INTO document_record
(household_id, title, type, issuer, document_date, expire_at, storage_location, metadata_json, created_at, updated_at)
VALUES
(@space_id, '冰箱保修卡', 'warranty', 'Demo Appliances', DATE_ADD(CURDATE(), INTERVAL -320 DAY), DATE_ADD(CURDATE(), INTERVAL 40 DAY), '家庭文件盒 A3', '{"demo":true,"serial":"LP-DEMO-FRIDGE"}', NOW(), NOW()),
(@space_id, '年度体检票据', 'receipt', 'Demo Clinic', DATE_ADD(CURDATE(), INTERVAL -20 DAY), NULL, '电子票据/2026', '{"demo":true,"amount":680}', NOW(), NOW());

INSERT INTO ai_call_log
(user_id, household_id, provider, scenario, prompt_hash, request_json, response_json, status, duration_ms, error_message, created_at, updated_at)
VALUES
(@user_id, @space_id, 'mock', 'parse_transaction', SHA2('demo transaction prompt', 256), '{"textLength":18}', '{"type":"expense","amount":128.00,"category":"餐饮"}', 'success', 42, NULL, DATE_ADD(NOW(), INTERVAL -3 DAY), DATE_ADD(NOW(), INTERVAL -3 DAY)),
(@user_id, @space_id, 'mock', 'parse_shopping', SHA2('demo shopping prompt', 256), '{"textLength":14}', '{"itemCount":4,"needsReview":true}', 'success', 38, NULL, DATE_ADD(NOW(), INTERVAL -2 DAY), DATE_ADD(NOW(), INTERVAL -2 DAY)),
(@user_id, @space_id, 'mock', 'parse_todo', SHA2('demo todo prompt', 256), '{"textLength":16}', '{"title":"缴纳水电费","priority":"urgent"}', 'success', 31, NULL, DATE_ADD(NOW(), INTERVAL -1 DAY), DATE_ADD(NOW(), INTERVAL -1 DAY)),
(@user_id, @space_id, 'mock', 'monthly_report', NULL, '{"year":YEAR(CURDATE()),"month":MONTH(CURDATE())}', '{"highlightCount":3,"suggestionCount":3}', 'success', 55, NULL, DATE_ADD(NOW(), INTERVAL -12 HOUR), DATE_ADD(NOW(), INTERVAL -12 HOUR)),
(@user_id, @space_id, 'mock', 'recommend_recipes', NULL, '{"inventoryItemCount":6,"recipeCount":3}', '{"recommendationCount":3}', 'success', 47, NULL, DATE_ADD(NOW(), INTERVAL -6 HOUR), DATE_ADD(NOW(), INTERVAL -6 HOUR)),
(@user_id, @space_id, 'mock', 'meal_plan_shopping_draft', NULL, '{"days":7}', '{"itemCount":3,"needsReview":true}', 'failed', 29, '演示失败日志：库存单位需要人工确认', DATE_ADD(NOW(), INTERVAL -2 HOUR), DATE_ADD(NOW(), INTERVAL -2 HOUR));

COMMIT;

SELECT
    @demo_email AS demo_email,
    @demo_password_note AS demo_password,
    @space_id AS demo_space_id,
    (SELECT COUNT(*) FROM transaction_record WHERE household_id = @space_id) AS transaction_count,
    (SELECT COUNT(*) FROM shopping_list WHERE household_id = @space_id) AS shopping_list_count,
    (SELECT COUNT(*) FROM inventory_item WHERE household_id = @space_id) AS inventory_item_count,
    (SELECT COUNT(*) FROM todo_task WHERE household_id = @space_id) AS todo_task_count,
    (SELECT COUNT(*) FROM recipe WHERE household_id = @space_id) AS recipe_count,
    (SELECT COUNT(*) FROM meal_plan WHERE household_id = @space_id) AS meal_plan_count,
    (SELECT COUNT(*) FROM ai_call_log WHERE household_id = @space_id) AS ai_call_log_count;
