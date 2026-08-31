ALTER TABLE batch
ADD COLUMN IF NOT EXISTS last_transaction_page_processed INTEGER NOT NULL DEFAULT 0;

ALTER TABLE batch
ADD CONSTRAINT chk_batch_last_transaction_page_processed_non_negative
CHECK (last_transaction_page_processed >= 0);


