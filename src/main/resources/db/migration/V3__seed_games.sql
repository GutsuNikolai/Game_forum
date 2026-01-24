INSERT INTO games (slug, title, description, cover_url, created_at, updated_at, rating_avg, rating_cnt)
VALUES
('cs2', 'Counter-Strike 2', 'Shooter game.', 'https://example.com/cs2.jpg', now(), now(), 0.0, 0),
('dota2', 'Dota 2', 'MOBA game.', 'https://example.com/dota2.jpg', now(), now(), 0.0, 0)
ON CONFLICT (slug) DO NOTHING;
