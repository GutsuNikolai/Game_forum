-- V1__init.sql (PostgreSQL)

-- 1) types
DO $$ BEGIN
  CREATE TYPE user_role AS ENUM ('USER', 'PUBLISHER', 'ADMIN');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TYPE review_status AS ENUM ('DRAFT', 'PUBLISHED');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- 2) users
CREATE TABLE IF NOT EXISTS users (
  id            BIGSERIAL PRIMARY KEY,
  username      VARCHAR(50)  NOT NULL UNIQUE,
  email         VARCHAR(120) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role          user_role    NOT NULL DEFAULT 'USER',
  created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- 3) games
CREATE TABLE IF NOT EXISTS games (
  id          BIGSERIAL PRIMARY KEY,
  slug        VARCHAR(80)  NOT NULL UNIQUE,   -- для URL типа /games/cs2
  title       VARCHAR(120) NOT NULL,
  description TEXT         NOT NULL DEFAULT '',
  cover_url   TEXT         NOT NULL DEFAULT '',
  created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),

  rating_avg  NUMERIC(3,2) NOT NULL DEFAULT 0.00, -- кэш (для скорости)
  rating_cnt  INTEGER      NOT NULL DEFAULT 0
);

-- 4) reviews (1 обзор на 1 игру, т.к. "единый публицист")
CREATE TABLE IF NOT EXISTS reviews (
  id           BIGSERIAL PRIMARY KEY,
  game_id      BIGINT      NOT NULL UNIQUE REFERENCES games(id) ON DELETE CASCADE,
  publisher_id BIGINT      NOT NULL REFERENCES users(id),
  title        VARCHAR(160) NOT NULL DEFAULT '',
  content      TEXT         NOT NULL DEFAULT '',
  status       review_status NOT NULL DEFAULT 'DRAFT',
  created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- 5) comments (только зарегистрированные)
CREATE TABLE IF NOT EXISTS comments (
  id         BIGSERIAL PRIMARY KEY,
  game_id    BIGINT     NOT NULL REFERENCES games(id) ON DELETE CASCADE,
  user_id    BIGINT     NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  content    TEXT       NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  is_deleted BOOLEAN    NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_comments_game_created ON comments(game_id, created_at DESC);

-- 6) ratings (1 оценка на игру на пользователя)
CREATE TABLE IF NOT EXISTS ratings (
  id         BIGSERIAL PRIMARY KEY,
  game_id    BIGINT    NOT NULL REFERENCES games(id) ON DELETE CASCADE,
  user_id    BIGINT    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  value      SMALLINT  NOT NULL CHECK (value >= 1 AND value <= 10),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (game_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_ratings_game ON ratings(game_id);
