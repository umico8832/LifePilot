CREATE TABLE IF NOT EXISTS recipe (
    id               BIGINT       PRIMARY KEY AUTO_INCREMENT,
    household_id     BIGINT       NOT NULL,
    name             VARCHAR(200) NOT NULL,
    description      TEXT         NULL,
    ingredients_json TEXT         NULL,
    steps_json       TEXT         NULL,
    created_by       BIGINT       NOT NULL,
    created_at       DATETIME(6)  NOT NULL,
    updated_at       DATETIME(6)  NOT NULL,
    CONSTRAINT fk_recipe_household FOREIGN KEY (household_id) REFERENCES household(id),
    CONSTRAINT fk_recipe_creator   FOREIGN KEY (created_by)   REFERENCES users(id)
);