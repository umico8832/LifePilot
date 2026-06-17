CREATE TABLE IF NOT EXISTS household (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(32) NOT NULL DEFAULT 'personal',
    owner_user_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_household_owner FOREIGN KEY (owner_user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS household_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    household_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(32) NOT NULL DEFAULT 'member',
    status VARCHAR(32) NOT NULL DEFAULT 'active',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_member_household FOREIGN KEY (household_id) REFERENCES household(id),
    CONSTRAINT fk_member_user FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE KEY uk_member_household_user (household_id, user_id)
);