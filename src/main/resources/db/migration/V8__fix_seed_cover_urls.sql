UPDATE games
SET cover_url = '/img/covers/cs2.jpg',
    updated_at = now()
WHERE slug = 'cs2'
  AND cover_url IS DISTINCT FROM '/img/covers/cs2.jpg';

UPDATE games
SET cover_url = '/img/covers/dota2.jpg',
    updated_at = now()
WHERE slug = 'dota2'
  AND cover_url IS DISTINCT FROM '/img/covers/dota2.jpg';
