CREATE TABLE IF NOT EXISTS meal_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    household_id BIGINT NOT NULL,
    recipe_id BIGINT NOT NULL,
    planned_date DATE NOT NULL,
    meal_type VARCHAR(20) NOT NULL,
    note VARCHAR(500),
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_meal_plan_household FOREIGN KEY (household_id) REFERENCES household(id),
    CONSTRAINT fk_meal_plan_recipe FOREIGN KEY (recipe_id) REFERENCES recipe(id),
    CONSTRAINT fk_meal_plan_creator FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE INDEX idx_meal_plan_household_date ON meal_plan(household_id, planned_date);