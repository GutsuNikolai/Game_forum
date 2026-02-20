INSERT INTO users (username, email, password_hash, role, created_at)
SELECT
    'admin',
    'admin@gameforum.local',
    '$2a$10$0d6Oj.WW1frO41HDEkaGS.XN22fJmoWx59qPowSjc10X.tnqXolk2',
    'ADMIN',
    now()
WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE username = 'admin'
       OR email = 'admin@gameforum.local'
);
