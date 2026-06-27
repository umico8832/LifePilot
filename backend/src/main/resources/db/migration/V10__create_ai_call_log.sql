CREATE TABLE ai_call_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    household_id BIGINT NOT NULL,
    provider VARCHAR(50) NOT NULL,
    scenario VARCHAR(80) NOT NULL,
    prompt_hash VARCHAR(64),
    request_json TEXT,
    response_json TEXT,
    status VARCHAR(20) NOT NULL,
    duration_ms BIGINT,
    error_message VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ai_call_log_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_ai_call_log_household FOREIGN KEY (household_id) REFERENCES household(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_ai_call_log_user_created ON ai_call_log(user_id, created_at);
CREATE INDEX idx_ai_call_log_household_created ON ai_call_log(household_id, created_at);
CREATE INDEX idx_ai_call_log_scenario_status ON ai_call_log(scenario, status);
