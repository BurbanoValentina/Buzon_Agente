CREATE TABLE complaint (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(64) NOT NULL UNIQUE,
    anonymous BOOLEAN NOT NULL,
    name VARCHAR(200),
    semester VARCHAR(50),
    category VARCHAR(80) NOT NULL,
    location_number VARCHAR(50),
    subject VARCHAR(200) NOT NULL,
    description VARCHAR(500) NOT NULL,
    status VARCHAR(30) NOT NULL,
    registered_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE
);
CREATE TABLE evidence (
    id BIGSERIAL PRIMARY KEY,
    complaint_id BIGINT NOT NULL REFERENCES complaint(id),
    file_name VARCHAR(255) NOT NULL,
    mime_type VARCHAR(50) NOT NULL,
    size_bytes BIGINT NOT NULL,
    drive_file_id VARCHAR(255) NOT NULL
);
CREATE TABLE status_history (
    id BIGSERIAL PRIMARY KEY,
    complaint_id BIGINT NOT NULL REFERENCES complaint(id),
    previous_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,
    comment VARCHAR(1000),
    changed_by VARCHAR(100) NOT NULL,
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE TABLE monthly_event (
    id BIGSERIAL PRIMARY KEY,
    complaint_id BIGINT NOT NULL REFERENCES complaint(id),
    event_type VARCHAR(40) NOT NULL,
    event_month DATE NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE TABLE administrator_account (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    security_answer_hash VARCHAR(255) NOT NULL,
    failed_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMP WITH TIME ZONE
);
CREATE TABLE audit_entry (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(80) NOT NULL,
    actor VARCHAR(100),
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    metadata JSONB
);
CREATE TABLE telegram_link (
    id BIGSERIAL PRIMARY KEY,
    telegram_account_hash VARCHAR(255) NOT NULL UNIQUE,
    linked_at TIMESTAMP WITH TIME ZONE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE security_challenge (
    id BIGSERIAL PRIMARY KEY,
    purpose VARCHAR(40) NOT NULL,
    value_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    consumed_at TIMESTAMP WITH TIME ZONE
);
CREATE TABLE word_report_draft (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
