CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    batch_id UUID NOT NULL,
    transaction_id VARCHAR(128) NOT NULL,
    batch_name VARCHAR(256) NOT NULL,
    payment_type VARCHAR(8) NOT NULL,
    batch_status VARCHAR(32) NOT NULL,
    account_holder_name VARCHAR(256) NOT NULL,
    transaction_amount NUMERIC(19, 2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_transaction_batch FOREIGN KEY (batch_id) REFERENCES batch (batch_id) ON DELETE CASCADE,
    CONSTRAINT uk_transaction_batch_transaction_id UNIQUE (batch_id, transaction_id)
);

CREATE INDEX idx_transactions_batch_id ON transactions (batch_id);
CREATE INDEX idx_transactions_transaction_id ON transactions (transaction_id);
CREATE INDEX idx_transactions_currency_code ON transactions (currency_code);

