CREATE TABLE IF NOT EXISTS job (
    job_id UUID PRIMARY KEY,
    user_id VARCHAR(128) NOT NULL,
    customer_name VARCHAR(256) NOT NULL,
    customer_agreement_id VARCHAR(128),
    payment_type VARCHAR(8) NOT NULL,
    date_from DATE NOT NULL,
    date_to DATE NOT NULL,
    accounts TEXT[] NOT NULL,
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
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_job_job_id UNIQUE (job_id),
    CONSTRAINT chk_job_status CHECK (status IN ('CREATED', 'FETCHING_BATCHES', 'BATCHES_FETCHED', 'FETCHING_TRANSACTIONS', 'TRANSACTIONS_FETCHED', 'GENERATING_CSV_LINK', 'CAN_BE_DOWNLOADED', 'FAILED')),
    CONSTRAINT chk_job_payment_type_ct_dd CHECK (payment_type IN ('CT', 'DD')),
    CONSTRAINT chk_job_date_range CHECK (date_from <= date_to),
    CONSTRAINT chk_job_accounts_non_empty CHECK (array_length(accounts, 1) > 0),
    CONSTRAINT chk_job_total_batches_non_negative CHECK (total_batches >= 0),
    CONSTRAINT chk_job_processed_batches_non_negative CHECK (processed_batches >= 0),
    CONSTRAINT chk_job_total_transactions_non_negative CHECK (total_transactions >= 0),
    CONSTRAINT chk_job_processed_transactions_non_negative CHECK (processed_transactions >= 0),
    CONSTRAINT chk_job_retry_count_non_negative CHECK (retry_count >= 0)
);

CREATE INDEX IF NOT EXISTS idx_job_status ON job (status);
CREATE INDEX IF NOT EXISTS idx_job_user_id ON job (user_id);


