ALTER TABLE reviews
  ALTER COLUMN status TYPE VARCHAR(20)
  USING status::text;

ALTER TABLE reviews
  ADD CONSTRAINT reviews_status_check
  CHECK (status IN ('DRAFT', 'PUBLISHED'));

-- если тип review_status больше нигде не нужен:
-- DROP TYPE IF EXISTS review_status;
