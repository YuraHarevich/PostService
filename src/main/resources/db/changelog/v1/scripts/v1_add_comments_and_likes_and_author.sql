ALTER TABLE post_schema.posts
    ADD COLUMN number_of_likes INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN number_of_comments INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN author VARCHAR(255) NOT NULL DEFAULT 'unknown';
ALTER TABLE post_schema.posts DROP COLUMN author_id;