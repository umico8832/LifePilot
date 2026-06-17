CREATE TABLE IF NOT EXISTS transaction_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    household_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(32) NOT NULL DEFAULT 'expense',
    icon VARCHAR(50) NULL,
    color VARCHAR(20) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_tcat_household FOREIGN KEY (household_id) REFERENCES household(id)
);

CREATE TABLE IF NOT EXISTS transaction_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    household_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    category_id BIGINT NULL,
    type VARCHAR(32) NOT NULL DEFAULT 'expense',
    amount DECIMAL(12,2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'CNY',
    occurred_at DATETIME(6) NOT NULL,
    merchant VARCHAR(200) NULL,
    note VARCHAR(500) NULL,
    source VARCHAR(32) NOT NULL DEFAULT 'manual',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_trans_household FOREIGN KEY (household_id) REFERENCES household(id),
    CONSTRAINT fk_trans_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_trans_category FOREIGN KEY (category_id) REFERENCES transaction_category(id)
);