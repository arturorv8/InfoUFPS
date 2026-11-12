-- =====================================================================
-- Migración V3: Cuentas, contenidos, etiquetas y comentarios
-- =====================================================================

-- 1. Cuentas
CREATE TABLE IF NOT EXISTS accounts (
    account_id    VARCHAR(36)  NOT NULL,
    name          VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(30)  NOT NULL,
    created_at    TIMESTAMP    NOT NULL,
    CONSTRAINT pk_accounts PRIMARY KEY (account_id)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_accounts_email ON accounts (email);

-- 2. Contenidos
CREATE TABLE IF NOT EXISTS contents (
    content_id     VARCHAR(36)  NOT NULL,
    title          VARCHAR(255) NOT NULL,
    subtitle       VARCHAR(255),
    description    TEXT,
    image_url      VARCHAR(512),
    category_tag   VARCHAR(50),
    target_url     VARCHAR(512) NOT NULL,
    likes_count    INTEGER      NOT NULL DEFAULT 0,
    comments_count INTEGER      NOT NULL DEFAULT 0,
    status         VARCHAR(30)  NOT NULL,
    module_type    VARCHAR(50)  NOT NULL,
    CONSTRAINT pk_contents PRIMARY KEY (content_id)
);

CREATE INDEX IF NOT EXISTS idx_contents_status ON contents (status);

-- 3. Etiquetas de contenido
CREATE TABLE IF NOT EXISTS content_tags (
    content_id VARCHAR(36) NOT NULL,
    tag        VARCHAR(50) NOT NULL
);

-- 4. Comentarios
CREATE TABLE IF NOT EXISTS comments (
    comment_id VARCHAR(36) NOT NULL,
    content_id VARCHAR(36) NOT NULL,
    author_id  VARCHAR(36) NOT NULL,
    text       TEXT        NOT NULL,
    created_at TIMESTAMP   NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT pk_comments PRIMARY KEY (comment_id)
);

CREATE INDEX IF NOT EXISTS idx_comments_content_id ON comments (content_id);
CREATE INDEX IF NOT EXISTS idx_comments_author_id ON comments (author_id);
CREATE INDEX IF NOT EXISTS idx_comments_content_author ON comments (content_id, author_id);
CREATE INDEX IF NOT EXISTS idx_comments_created_at ON comments (created_at);
