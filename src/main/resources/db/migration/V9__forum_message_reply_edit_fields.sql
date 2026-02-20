ALTER TABLE forum_messages
    ADD COLUMN IF NOT EXISTS parent_message_id BIGINT REFERENCES forum_messages(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS quoted_message_id BIGINT REFERENCES forum_messages(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS edited_at TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS idx_forum_messages_parent ON forum_messages(parent_message_id);
CREATE INDEX IF NOT EXISTS idx_forum_messages_quoted ON forum_messages(quoted_message_id);
