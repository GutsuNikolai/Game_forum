-- Меняем тип колонки role на varchar
ALTER TABLE users
  ALTER COLUMN role TYPE VARCHAR(20)
  USING role::text;

-- (опционально) добавим простую проверку, чтобы не было мусора
ALTER TABLE users
  ADD CONSTRAINT users_role_check
  CHECK (role IN ('USER', 'PUBLISHER', 'ADMIN'));

-- (опционально) если больше нигде user_role не используется — можно удалить тип
-- DROP TYPE IF EXISTS user_role;
