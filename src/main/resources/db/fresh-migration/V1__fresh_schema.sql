-- Créditos: oEnzoRibas
-- Fresh lineage only: empty, dedicated MariaDB 10.7+ database.
-- Not an upgrade of db/migration. Do not baseline an empty database.
-- Expected before: no application tables or Flyway history.
-- Expected after: five final tables; no imported users or legacy data.
-- No foreign keys: the current domain has no entity relationships.
CREATE TABLE users (
    id UUID NOT NULL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    role VARCHAR(16) NOT NULL DEFAULT 'USER',
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE posts (
    id UUID NOT NULL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    tags VARCHAR(2048) NOT NULL DEFAULT '',
    created_at TIMESTAMP(6) NOT NULL,
    image LONGBLOB,
    image_content_type VARCHAR(100),
    INDEX idx_posts_created_at_id (created_at, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE episodes (
    id UUID NOT NULL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    youtube_id VARCHAR(11) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    video_url VARCHAR(255) NOT NULL,
    thumbnail_url VARCHAR(512),
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_episodes_youtube_id UNIQUE (youtube_id),
    INDEX idx_episodes_created_at_id (created_at, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE contact_messages (
    id UUID NOT NULL PRIMARY KEY,
    sender_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    INDEX idx_contact_messages_created_at_id (created_at, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE topic_suggestions (
    id UUID NOT NULL PRIMARY KEY,
    sender_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    topic VARCHAR(2000) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    INDEX idx_topic_suggestions_created_at_id (created_at, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
