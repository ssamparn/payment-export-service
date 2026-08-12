CREATE TABLE IF NOT EXISTS job (
    job_id UUID PRIMARY KEY,
    user_id VARCHAR(128) NOT NULL,
    customer_name VARCHAR(256) NOT NULL,
    customer_agreement_id VARCHAR(128),
    job_type VARCHAR(8) NOT NULL,
    payment_type VARCHAR(8) NOT NULL,
    date_from DATE NOT NULL,
    date_to DATE NOT NULL,
    account_ibans TEXT[] NOT NULL,
    account_currency_codes TEXT[] NOT NULL,
    jwt_token TEXT NOT NULL,
    status VARCHAR(64) NOT NULL,
    total_batches INTEGER NOT NULL DEFAULT 0,
    processed_batches INTEGER NOT NULL DEFAULT 0,
    total_transactions INTEGER NOT NULL DEFAULT 0,
    processed_transactions INTEGER NOT NULL DEFAULT 0,
    last_batch_page_processed INTEGER NOT NULL DEFAULT 0,
    retry_count INTEGER NOT NULL DEFAULT 0,
    last_error TEXT,
    csv_file_location VARCHAR(1024),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(128) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_job_status ON job (status);
CREATE INDEX IF NOT EXISTS idx_job_user_id ON job (user_id);

