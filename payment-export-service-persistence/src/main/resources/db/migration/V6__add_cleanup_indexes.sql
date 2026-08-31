CREATE INDEX IF NOT EXISTS idx_job_status_created_at ON job (status, created_at);
CREATE INDEX IF NOT EXISTS idx_job_status_updated_at ON job (status, updated_at);
CREATE INDEX IF NOT EXISTS idx_batch_status_updated_at ON batch (status, updated_at);
CREATE INDEX IF NOT EXISTS idx_transactions_updated_at ON transactions (updated_at);

