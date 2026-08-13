CREATE TABLE IF NOT EXISTS batch (
    batch_id UUID PRIMARY KEY,
    job_id UUID NOT NULL,
    internal_batch_id VARCHAR(128) NOT NULL,
    iban VARCHAR(34),
    currency_code VARCHAR(3),
    payment_type VARCHAR(8) NOT NULL,
    status VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_batch_job FOREIGN KEY (job_id) REFERENCES job (job_id) ON DELETE CASCADE,
    CONSTRAINT uk_batch_job_internal_batch_id UNIQUE (job_id, internal_batch_id),
    CONSTRAINT chk_batch_internal_batch_id_not_blank CHECK (char_length(trim(internal_batch_id)) > 0),
    CONSTRAINT chk_batch_payment_type_ct_dd CHECK (payment_type IN ('CT', 'DD')),
    CONSTRAINT chk_batch_status CHECK (status IN ('CREATED', 'PROCESSING', 'COMPLETED', 'FAILED')),
    CONSTRAINT chk_batch_iban_not_blank CHECK (iban IS NULL OR char_length(trim(iban)) > 0),
    CONSTRAINT chk_batch_currency_code_uppercase CHECK (currency_code IS NULL OR currency_code = upper(currency_code)),
    CONSTRAINT chk_batch_iban_currency_presence CHECK (
        (iban IS NULL AND currency_code IS NULL)
        OR (iban IS NOT NULL AND currency_code IS NOT NULL)
    ),
    CONSTRAINT chk_batch_updated_after_created CHECK (updated_at >= created_at)
);

CREATE INDEX IF NOT EXISTS idx_batch_job_id ON batch (job_id);
CREATE INDEX IF NOT EXISTS idx_batch_internal_batch_id ON batch (internal_batch_id);
CREATE INDEX IF NOT EXISTS idx_batch_status ON batch (status);
CREATE INDEX IF NOT EXISTS idx_batch_job_status ON batch (job_id, status);

