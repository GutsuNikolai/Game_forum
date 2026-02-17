CREATE TABLE IF NOT EXISTS forum_topics (
  id               BIGSERIAL PRIMARY KEY,
  game_id          BIGINT       NOT NULL REFERENCES games(id) ON DELETE CASCADE,
  title            VARCHAR(120) NOT NULL,
  description      VARCHAR(1000) NOT NULL,
  author           VARCHAR(80)  NOT NULL,
  replies          INTEGER      NOT NULL DEFAULT 0,
  views            INTEGER      NOT NULL DEFAULT 0,
  icon             VARCHAR(40)  NOT NULL DEFAULT 'fas fa-comments',
  created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
  last_activity_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_forum_topics_game ON forum_topics(game_id);
CREATE INDEX IF NOT EXISTS idx_forum_topics_game_last_activity ON forum_topics(game_id, last_activity_at DESC);

CREATE TABLE IF NOT EXISTS forum_messages (
  id              BIGSERIAL PRIMARY KEY,
  topic_id        BIGINT      NOT NULL REFERENCES forum_topics(id) ON DELETE CASCADE,
  author          VARCHAR(80) NOT NULL,
  avatar_color    VARCHAR(20) NOT NULL,
  content         TEXT        NOT NULL DEFAULT '',
  image_urls_text TEXT        NOT NULL DEFAULT '',
  likes           INTEGER     NOT NULL DEFAULT 0,
  replies         INTEGER     NOT NULL DEFAULT 0,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_forum_messages_topic ON forum_messages(topic_id, id);
