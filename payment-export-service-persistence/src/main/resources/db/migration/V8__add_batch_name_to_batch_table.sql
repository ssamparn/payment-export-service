ALTER TABLE batch
ADD COLUMN IF NOT EXISTS batch_name VARCHAR(256);

-- Backfill from already persisted transaction rows when available.
UPDATE batch b
SET batch_name = source.batch_name
FROM (
    SELECT t.batch_id, MAX(t.batch_name) AS batch_name
    FROM transactions t
    WHERE t.batch_name IS NOT NULL
      AND char_length(trim(t.batch_name)) > 0
    GROUP BY t.batch_id
) source
WHERE b.batch_id = source.batch_id
  AND (b.batch_name IS NULL OR char_length(trim(b.batch_name)) = 0);

-- Fallback for any remaining legacy rows with no transaction-derived batch name.
UPDATE batch
SET batch_name = internal_batch_id
WHERE batch_name IS NULL OR char_length(trim(batch_name)) = 0;

ALTER TABLE batch
ALTER COLUMN batch_name SET NOT NULL;

ALTER TABLE batch
DROP CONSTRAINT IF EXISTS chk_batch_name_not_blank;

ALTER TABLE batch
ADD CONSTRAINT chk_batch_name_not_blank CHECK (char_length(trim(batch_name)) > 0);

