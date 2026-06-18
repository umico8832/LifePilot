CREATE TABLE IF NOT EXISTS todo_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    household_id BIGINT NOT NULL,
    title VARCHAR(300) NOT NULL,
    description TEXT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    priority VARCHAR(20) NOT NULL DEFAULT 'medium',
    due_at DATETIME(6) NULL,
    repeat_rule VARCHAR(100) NULL,
    assigned_to BIGINT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_todo_household FOREIGN KEY (household_id) REFERENCES household(id),
    CONSTRAINT fk_todo_assigned FOREIGN KEY (assigned_to) REFERENCES users(id)
);