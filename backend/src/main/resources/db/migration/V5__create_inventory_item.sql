CREATE TABLE IF NOT EXISTS inventory_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    household_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    category VARCHAR(100) NULL,
    quantity DECIMAL(10,2) NOT NULL DEFAULT 0,
    unit VARCHAR(50) NULL,
    location VARCHAR(200) NULL,
    expire_at DATETIME(6) NULL,
    low_stock_threshold DECIMAL(10,2) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_invitem_household FOREIGN KEY (household_id) REFERENCES household(id)
);