CREATE TABLE IF NOT EXISTS shopping_list (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    household_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'active',
    estimated_budget DECIMAL(12,2) NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_slist_household FOREIGN KEY (household_id) REFERENCES household(id),
    CONSTRAINT fk_slist_user FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS shopping_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    shopping_list_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    quantity DECIMAL(10,2) NOT NULL DEFAULT 1,
    unit VARCHAR(50) NULL,
    estimated_price DECIMAL(12,2) NULL,
    purchased TINYINT(1) NOT NULL DEFAULT 0,
    inventory_item_id BIGINT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_sitem_list FOREIGN KEY (shopping_list_id) REFERENCES shopping_list(id)
);